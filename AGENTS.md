# Kestra Telegram Plugin

## What

- Provides plugin components under `io.kestra.plugin.telegram`.
- Includes classes such as `TelegramTemplate`, `TelegramBotApiService`, `TelegramExecution`, `TelegramSend`.

## Why

- This plugin integrates Kestra with Telegram.
- It provides tasks that send notifications via Telegram bots.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `telegram`

Infrastructure dependencies (Docker Compose services):

- `app`

### Key Plugin Classes

- `io.kestra.plugin.telegram.TelegramExecution`
- `io.kestra.plugin.telegram.TelegramSend`

### Project Structure

```
plugin-telegram/
├── src/main/java/io/kestra/plugin/telegram/
├── src/test/java/io/kestra/plugin/telegram/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
