package com.weatherbot.climatebot.controller;

import com.weatherbot.climatebot.entity.Subscriber;
import com.weatherbot.climatebot.repository.SubscriberRepository;
import com.weatherbot.climatebot.utility.EmojiUtility;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WeatherBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.bot.token}")
    private String token;

    final Set<Long> awaitingLocation = ConcurrentHashMap.newKeySet();

    @Value("${weather.token}")
    private String weatherToken;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Override
    public void onRegister() {
        List<BotCommand> commandList = List.of(
                new BotCommand("/start", "Start the bot"),
                new BotCommand("/weather", "Get current weather"),
                new BotCommand("/subscribe", "Subscribe for weather updates"),
                new BotCommand("/unsubscribe", "Unsubscribe from updates"),
                new BotCommand("/updatelocation", "Update the saved location")
        );

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commandList);
        setMyCommands.setScope(new BotCommandScopeDefault());

        try {
            this.execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.info("Exception occurred");
        }
    }

    @Autowired
    RestTemplate restTemplate;

    public static String data = "";


    private final Set<Long> awaitingLocationForSubscription = ConcurrentHashMap.newKeySet();
    private final Set<Long> awaitingLocationForWeather = ConcurrentHashMap.newKeySet();
    private final Set<Long> awaitingLocationUpdate = ConcurrentHashMap.newKeySet();


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();

            // Handle commands
            if (update.getMessage().hasText()) {
                String msgText = update.getMessage().getText();
                String user = update.getMessage().getFrom().getFirstName();

                switch (msgText) {
                    case "/start":
                        String response = "Hello " + user + "\nWelcome to Weather Bot ☀\uFE0F";
                        sendMessage(chatId, response);
                        break;

                    case "/weather":
                        Optional<Subscriber> optionalSub = subscriberRepository.findByChatId(chatId);
                        if (optionalSub.isPresent()) {
                            Subscriber sub = optionalSub.get();
                            String weather = getWeatherByCoordinates(sub.getLatitude(), sub.getLongitude());
                            sendMessage(chatId, weather);
                        } else {
                            sendLocationRequest(chatId);
                            awaitingLocationForWeather.add(chatId);
                        }
                        break;


                    case "/subscribe":
                        if (!subscriberRepository.existsByChatId(chatId)) {
                            sendLocationRequest(chatId);
                            awaitingLocationForSubscription.add(chatId);
                        } else {
                            sendMessage(chatId, "ℹ️ You are already subscribed.");
                        }
                        break;
                    case "/unsubscribe":
                        Optional<Subscriber> optionalSub1 = subscriberRepository.findByChatId(chatId);
                        if (optionalSub1.isPresent()) {
                            subscriberRepository.deleteByChatId(chatId);
                            sendMessage(chatId, "❌ You have been unsubscribed from weather updates.");
                        } else {
                            sendMessage(chatId, "ℹ️ You are not subscribed.");
                        }
                        break;
                    case "/updatelocation":
                        if (subscriberRepository.existsByChatId(chatId)) {
                            sendLocationRequest(chatId);
                            awaitingLocationUpdate.add(chatId); // Mark user waiting for location update
                        } else {
                            sendMessage(chatId, "❗ You are not subscribed yet. Please use /subscribe first.");
                        }
                        break;
                }
            }

            // Handle location
            if (update.getMessage().hasLocation()) {
                double lat = update.getMessage().getLocation().getLatitude();
                double lon = update.getMessage().getLocation().getLongitude();

                // If user was subscribing
                if (awaitingLocationForSubscription.contains(chatId)) {
                    awaitingLocationForSubscription.remove(chatId);

                    Subscriber sub = new Subscriber();
                    sub.setChatId(chatId);
                    sub.setFirstName(update.getMessage().getFrom().getFirstName());
                    sub.setLastName(update.getMessage().getFrom().getLastName());
                    sub.setUsername(update.getMessage().getFrom().getUserName());
                    sub.setLatitude(lat);
                    sub.setLongitude(lon);

                    subscriberRepository.save(sub);
                    sendMessage(chatId, "\uD83D\uDCCD Location saved! You're now subscribed for weather updates every 6 hours.");
                    return;
                }

                // If user requested weather
                else if (awaitingLocationForWeather.contains(chatId)) {
                    awaitingLocationForWeather.remove(chatId);

                    String weather = getWeatherByCoordinates(lat, lon);
                    sendMessage(chatId, weather);
                    return;
                } else if (awaitingLocationUpdate.contains(chatId)) {
                    awaitingLocationUpdate.remove(chatId);
                    Optional<Subscriber> subOpt = subscriberRepository.findByChatId(chatId);
                    if (subOpt.isPresent()) {
                        Subscriber sub = subOpt.get();
                        sub.setLatitude(lat);
                        sub.setLongitude(lon);
                        subscriberRepository.save(sub);
                        sendMessage(chatId, "\uD83D\uDCCD Location updated successfully!");
                    } else {
                        sendMessage(chatId, "⚠️ Unexpected error: subscriber not found.");
                    }
                    return;
                }


                // If no reason for location
                sendMessage(chatId, "📍 Received location, but no command was issued.");
            }
        }
    }


    public void sendMessage(Long chatId, String response) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(response);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.info("Exception occurred while Sending msg");
        }
    }

    private void sendLocationRequest(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please share your location to get the weather report:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton locationButton = new KeyboardButton("📍 Send Location");
        locationButton.setRequestLocation(true);
        row.add(locationButton);

        keyboardMarkup.setKeyboard(java.util.Collections.singletonList(row));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.info("Exception occurred");
        }
    }

    public String getWeatherByCoordinates(double lat, double lon) {
        String url = "http://api.weatherstack.com/current?access_key=" + weatherToken +
                "&query=" + lat + "," + lon;

        try {
            // Call the API using RestTemplate
            String response = restTemplate.getForObject(url, String.class);
            data = response;
            // Parse JSON using org.json
            JSONObject json = new JSONObject(response);
            if (json.has("current")) {
                String temp = json.getJSONObject("current").get("temperature").toString();
                String weatherDesc = json.getJSONObject("current").getJSONArray("weather_descriptions").getString(0);
                String cityName = json.getJSONObject("location").getString("name");
                int UVIndex = json.getJSONObject("current").getInt("uv_index");
                String feelsLike = json.getJSONObject("current").get("feelslike").toString();
                int humidity = json.getJSONObject("current").getInt("humidity");
                JSONObject airQuality = json.getJSONObject("current").getJSONObject("air_quality");
                int usAqiIndex = airQuality.getInt("us-epa-index");
                String emoji = EmojiUtility.getWeatherEmoji(weatherDesc);
                String feelsLikeEmoji = EmojiUtility.getFeelsLikeEmoji(Integer.parseInt(feelsLike));
                String UVEmoji = EmojiUtility.getUVEmoji(UVIndex);

                return cityName + "\uD83D\uDCCD \n" +
                        "Temperature : " + temp + "°C \uD83C\uDF21\uFE0F\n" +
                        "Condition: " + weatherDesc + " " + emoji + "\n" +
                        "UV Index: " + UVIndex + " " + UVEmoji + "\n" +
                        "Air Quality: " + EmojiUtility.getAQIEmoji(usAqiIndex) + "\n" +
                        "Humidity: " + humidity + "% " + EmojiUtility.getHumidityEmoji(humidity) + "\n" +
                        "Feels Like: " + feelsLike + "°C " + feelsLikeEmoji;

            } else {
                return "Couldn't fetch weather for your location.";
            }

        } catch (Exception e) {
            log.info("Exception occurred");
            return "Error fetching weather data.";
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
