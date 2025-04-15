package com.weatherbot.climatebot.service;

import com.weatherbot.climatebot.controller.WeatherBot;
import com.weatherbot.climatebot.entity.Subscriber;
import com.weatherbot.climatebot.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WeatherScheduler {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private WeatherBot weatherBot;

    // Run every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void sendScheduledWeather() {
        for (Subscriber sub : subscriberRepository.findAll()) {
            String weatherReport = weatherBot.getWeatherByCoordinates(sub.getLatitude(), sub.getLongitude());
            weatherBot.sendMessage(sub.getChatId(), weatherReport);
        }
    }

}
