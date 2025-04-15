package com.weatherbot.climatebot.utility;

public class EmojiUtility {
    public static String getWeatherEmoji(String condition) {
        condition = condition.toLowerCase();

        if (condition.contains("sun") || condition.contains("clear")) {
            return "☀️";
        } else if (condition.contains("cloud")) {
            return "☁️";
        } else if (condition.contains("rain")) {
            return "🌧️";
        } else if (condition.contains("storm") || condition.contains("thunder")) {
            return "⛈️";
        } else if (condition.contains("snow")) {
            return "❄️";
        } else if (condition.contains("mist") || condition.contains("fog")) {
            return "🌫️";
        } else if (condition.contains("wind")) {
            return "💨";
        } else {
            return "🌈";
        }
    }

    public static String getFeelsLikeEmoji(int feelsLike) {
        if (feelsLike <= 0) return "🥶❄️";
        else if (feelsLike <= 15) return "🧥🍂";
        else if (feelsLike <= 25) return "🙂🌤️";
        else if (feelsLike <= 32) return "😅☀️";
        else if (feelsLike <= 40) return "🥵🔥";
        else return "🌡️🔥⚠️";
    }

    public static String getUVEmoji(int uvIndex) {
        if (uvIndex <= 2) return "🟢";
        else if (uvIndex <= 5) return "🟡";
        else if (uvIndex <= 7) return "🟠☀️";
        else if (uvIndex <= 10) return "🔴🌞";
        else return "🟣⚠️🌞";
    }

    public static String getHumidityEmoji(int humidity) {
        if (humidity < 30) {
            return "💧"; // dry
        } else if (humidity < 60) {
            return "💦"; // moderate
        } else {
            return "💦"; // high humidity
        }
    }

    public static String getAQIEmoji(int pm25) {
        if (pm25 <= 30) {
           return "50🌿  Good 🟢😌";
        } else if (pm25 <= 60) {
            return "100🌿 Satisfactory 💚🙂";
        } else if (pm25 <= 90) {
            return "200🌿 Moderate 🟡😷";
        } else if (pm25 <= 120) {
            return "300🌿 Poor 🟠🤒";
        } else if (pm25 <= 250) {
            return "400🌿 Very Poor 🔴😖";
        } else {
            return "500 Severe 🟣☠️";
        }
    }
}
