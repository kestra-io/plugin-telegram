package io.kestra.plugin.telegram;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
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
    title = "Send a Telegram message."
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
                    payload: |
                      {
                        "text": "Telegram Alert"
                      }
                """
        )
    },
    aliases = "io.kestra.plugin.notifications.telegram.TelegramSend"
)
public class TelegramSend extends AbstractTelegramConnection {
    private static final String TELEGRAMAPI_BASE_URL = "https://api.telegram.org";

    @Schema(title = "Telegram Bot token")
    @NotNull
    protected Property<String> token;

    @Schema(title = "Telegram channel/user ID")
    @NotNull
    protected Property<String> channel;

    @Schema(title = "Message payload")
    protected Property<String> payload;

    @Schema(title = "Telegram Bot parse-Mode", description = "Optional text formatting mode for Telegram messages. Supported values: HTML, Markdown, MarkdownV2.", example = "HTML")
    @Nullable
    protected Property<ParseMode> parseMode;
    @Schema(
        title = "Only to be used when testing locally"
    )
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
    }
}
