# 🌤️ Cloudy - Telegram Weather Assistant

**ClimateBot** is a Telegram bot that provides real-time weather updates, periodic weather reports, air quality (AQI), and precipitation chances for user-defined locations.

Built with:
- 🔧 Spring Boot (Java)
- ☁️ WeatherStack API
- 💬 Telegram Bot API
- 🗃️ MongoDB (for user subscription management)

---

## 🚀 Features

- `/weather` → Get current weather for any city
- `/subscribe` → Subscribe to automatic weather updates every 6 hours
- `/unsubscribe` → Stop receiving automated reports
- `/updatelocation <new_location>` → Update your subscribed location
- ✅ Shows:
  - Temperature 🌡️
  - Weather description ☁️
  - AQI level (Indian Scale 🇮🇳) 💨
  - Precipitation % 🌧️
  - Emojis to make it friendlier ✨

---

## 🛠️ Setup

### 1. Clone the repo
```bash
git clone https://github.com/yourusername/climatebot.git
cd climatebot
