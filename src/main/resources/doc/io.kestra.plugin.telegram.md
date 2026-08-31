# How to use the Telegram plugin

Send messages and execution summaries to Telegram chats via the Telegram Bot API.

## Authentication

Set `token` to your Telegram bot token (obtained from [@BotFather](https://t.me/botfather)) and `channel` to the target chat ID or channel username. Store `token` in a [secret](https://kestra.io/docs/concepts/secret).

## Tasks

`TelegramSend` sends a message as a step within a flow. Set `payload` directly to the message text. Plain text works without a `parseMode`; for formatted messages, set `parseMode` to `HTML` or `MarkdownV2` and use the corresponding formatting in `payload`.

`TelegramExecution` sends a structured execution summary including status, duration, and an execution link, and is designed for use with a [Flow trigger](https://kestra.io/docs/workflow-components/triggers) in a dedicated monitoring namespace that watches other namespaces for failures.
