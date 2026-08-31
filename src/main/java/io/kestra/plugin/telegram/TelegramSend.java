package io.kestra.plugin.telegram;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Send a Telegram chat message",
    description = "Posts a Telegram `sendMessage` request using a Bot token and chat ID. The payload is the message text (plain text or HTML/MarkdownV2 markup); default parse mode sends plain text. Supports HTTP client overrides and an optional API endpoint override for local testing."
)
@Plugin(
    examples = {
        @Example(
            title = "Send a Telegram message on a failed flow execution.",
            full = true,
            code = """
                id: unreliable_flow
                namespace: company.team

                tasks:
                  - id: fail
                    type: io.kestra.plugin.scripts.shell.Commands
                    runner: PROCESS
                    commands:
                      - exit 1

                errors:
                  - id: alert_on_failure
                    type: io.kestra.plugin.telegram.TelegramSend
                    token: "{{ secret('TELEGRAM_TOKEN') }}" # format: 6090305634:xyz
                    channel: "2072728690"
                    parseMode: HTML
                    payload: "<b>Telegram Alert</b>"
                """
        )
    },
    aliases = "io.kestra.plugin.notifications.telegram.TelegramSend"
)
public class TelegramSend extends AbstractTelegramConnection {
    private static final String TELEGRAMAPI_BASE_URL = "https://api.telegram.org";

    @Schema(title = "Bot access token", description = "Telegram Bot API token; store as a secret and avoid hardcoding.")
    @NotNull
    @PluginProperty(secret = true, group = "main")
    @ToString.Exclude
    protected Property<String> token;

    @Schema(title = "Chat ID or channel ID", description = "Target chat identifier for the user or channel; supports expressions.")
    @NotNull
    @PluginProperty(group = "main")
    protected Property<String> channel;

    @Schema(title = "Message payload", description = "Message text sent as Telegram `sendMessage` `text` (plain text, or HTML/MarkdownV2 markup when parseMode is set). Do not wrap the value in a JSON object.")
    @PluginProperty(group = "main")
    protected Property<String> payload;

    @Schema(title = "Telegram Bot parse-Mode", description = "Optional text formatting mode. Use the case-sensitive enum names HTML or MARKDOWNV2 (values sent to Telegram are HTML and MarkdownV2). Default sends plain text.", example = "MARKDOWNV2")
    @Nullable
    @PluginProperty(group = "advanced")
    protected Property<ParseMode> parseMode;
    @Schema(
        title = "Custom Telegram API base URL",
        description = "Override the Telegram API endpoint for local testing; defaults to https://api.telegram.org."
    )
    @PluginProperty(group = "connection")
    protected Property<String> endpointOverride;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        String rEndpointOverride = runContext.render(this.endpointOverride).as(String.class).orElse(TELEGRAMAPI_BASE_URL);

        HttpRequest.HttpRequestBuilder requestBuilder = createRequestBuilder(runContext);

        try (HttpClient httpClient = new HttpClient(runContext, super.httpClientConfigurationWithOptions())) {
            String rDestination = runContext.render(this.channel).as(String.class).orElseThrow();
            String rApiToken = runContext.render(this.token).as(String.class).orElseThrow();
            String rPayload = runContext.render(payload).as(String.class).orElseThrow();
            String rParseMode = runContext.render(this.parseMode).as(ParseMode.class).map(ParseMode::getValue).orElse(null);
            TelegramBotApiService.send(httpClient, rDestination, rApiToken, rPayload, rEndpointOverride, requestBuilder, rParseMode);
        }

        return null;
    }

    public enum ParseMode {
        HTML("HTML"),
        MARKDOWNV2("MarkdownV2");

        private final String value;

        ParseMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static ParseMode fromString(String value) {
            if (value == null) {
                return null;
            }
            for (ParseMode parseMode : ParseMode.values()) {
                if (parseMode.name().equals(value)) {
                    return parseMode;
                }
            }
            throw new IllegalArgumentException(
                "Invalid parseMode value '" + value + "'. Valid values (case-sensitive): " +
                    java.util.Arrays.stream(ParseMode.values()).map(Enum::name).collect(java.util.stream.Collectors.joining(", "))
            );
        }
    }
}
