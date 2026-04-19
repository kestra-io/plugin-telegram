# Kestra Telegram Plugin

## What

- Provides plugin components under `io.kestra.plugin.telegram`.
- Includes classes such as `TelegramTemplate`, `TelegramBotApiService`, `TelegramExecution`, `TelegramSend`.

## Why

- What user problem does this solve? Teams need to send notifications via Telegram bots from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Telegram steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Telegram.

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
