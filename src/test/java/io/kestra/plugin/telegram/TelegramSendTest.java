package io.kestra.plugin.telegram;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.serializers.JacksonMapper;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToObject;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test will only test the main task, this allow you to send any input
 * parameters to your task and test the returning behaviour easily.
 */
@KestraTest
class TelegramSendTest {

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private RunContextFactory runContextFactory;

    @BeforeEach
    @AfterEach
    void resetMock() {
        FakeTelegramController.message = null;
        FakeTelegramController.token = null;
    }

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of();

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String message = "Hello";
        String channel = "channel";
        String token = "token";

        TelegramSend task = TelegramSend.builder()
            .endpointOverride(Property.ofValue(embeddedServer.getURL().toString()))
            .token(Property.ofValue(token))
            .channel(Property.ofValue(channel))
            .payload(Property.ofValue(message))
            .build();
        task.run(runContext);

        assertThat(FakeTelegramController.token, containsString(token));
        assertThat(FakeTelegramController.message, equalToObject(new TelegramBotApiService.TelegramMessage(channel, message, null)));

    }

    @Test
    void run_withParseModeAsHTML_shouldSendTelegram() throws Exception {
        RunContext runContext = runContextFactory.of();

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String message = "Hello";
        String channel = "channel";
        String token = "token";
        String parseMode = TelegramSend.ParseMode.HTML.getValue();

        TelegramSend task = TelegramSend.builder()
            .endpointOverride(Property.ofValue(embeddedServer.getURL().toString()))
            .token(Property.ofValue(token))
            .channel(Property.ofValue(channel))
            .payload(Property.ofValue(message))
            .parseMode(Property.ofValue(TelegramSend.ParseMode.HTML))
            .build();
        task.run(runContext);

        assertThat(FakeTelegramController.token, containsString(token));
        assertThat(FakeTelegramController.message, equalToObject(new TelegramBotApiService.TelegramMessage(channel, message, parseMode)));

    }

    @Test
    void run_withParseModeAsMARKDOWNV2_shouldSendTelegram() throws Exception {
        RunContext runContext = runContextFactory.of();

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String message = "Hello";
        String channel = "channel";
        String token = "token";
        String parseMode = TelegramSend.ParseMode.MARKDOWNV2.getValue();

        TelegramSend task = TelegramSend.builder()
            .endpointOverride(Property.ofValue(embeddedServer.getURL().toString()))
            .token(Property.ofValue(token))
            .channel(Property.ofValue(channel))
            .payload(Property.ofValue(message))
            .parseMode(Property.ofValue(TelegramSend.ParseMode.MARKDOWNV2))
            .build();
        task.run(runContext);

        assertThat(FakeTelegramController.token, containsString(token));
        assertThat(FakeTelegramController.message, equalToObject(new TelegramBotApiService.TelegramMessage(channel, message, parseMode)));

    }

    @Test
    void run_withParseModeAsMARKDOWNV2_fromYaml_shouldSendTelegram() throws Exception {
        RunContext runContext = runContextFactory.of();

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String message = "Hello";
        String channel = "channel";
        String token = "token";

        String yaml = """
            endpointOverride: "%s"
            token: "%s"
            channel: "%s"
            payload: "%s"
            parseMode: MARKDOWNV2
            """.formatted(embeddedServer.getURL().toString(), token, channel, message);

        TelegramSend task = JacksonMapper.ofYaml().readValue(yaml, TelegramSend.class);
        task.run(runContext);

        assertThat(FakeTelegramController.token, containsString(token));
        assertThat(FakeTelegramController.message, equalToObject(new TelegramBotApiService.TelegramMessage(channel, message, "MarkdownV2")));
    }

    @Test
    void run_withParseModeAsHTML_fromYaml_shouldSendTelegram() throws Exception {
        RunContext runContext = runContextFactory.of();

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String message = "Hello";
        String channel = "channel";
        String token = "token";

        String yaml = """
            endpointOverride: "%s"
            token: "%s"
            channel: "%s"
            payload: "%s"
            parseMode: HTML
            """.formatted(embeddedServer.getURL().toString(), token, channel, message);

        TelegramSend task = JacksonMapper.ofYaml().readValue(yaml, TelegramSend.class);
        task.run(runContext);

        assertThat(FakeTelegramController.token, containsString(token));
        assertThat(FakeTelegramController.message, equalToObject(new TelegramBotApiService.TelegramMessage(channel, message, "HTML")));
    }

    @Test
    void run_withParseModeAsMarkdownV2_fromYaml_shouldThrowDescriptiveException() throws Exception {
        RunContext runContext = runContextFactory.of();

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String yaml = """
            endpointOverride: "%s"
            token: "token"
            channel: "channel"
            payload: "Hello"
            parseMode: MarkdownV2
            """.formatted(embeddedServer.getURL().toString());

        TelegramSend task = JacksonMapper.ofYaml().readValue(yaml, TelegramSend.class);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.run(runContext)
        );

        assertThat(
            exception.getMessage(),
            equalToObject("Invalid parseMode value 'MarkdownV2'. Valid values (case-sensitive): HTML, MARKDOWNV2")
        );
    }

    @Test
    void run_withParseModeAsExpression_shouldSendTelegram() throws Exception {
        RunContext runContext = runContextFactory.of(Map.of("mode", "MARKDOWNV2"));

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String message = "Hello";
        String channel = "channel";
        String token = "token";

        String yaml = """
            endpointOverride: "%s"
            token: "%s"
            channel: "%s"
            payload: "%s"
            parseMode: "{{ mode }}"
            """.formatted(embeddedServer.getURL().toString(), token, channel, message);

        TelegramSend task = JacksonMapper.ofYaml().readValue(yaml, TelegramSend.class);
        task.run(runContext);

        assertThat(FakeTelegramController.token, containsString(token));
        assertThat(FakeTelegramController.message, equalToObject(new TelegramBotApiService.TelegramMessage(channel, message, "MarkdownV2")));
    }

    @Test
    void run_withParseModeAsExpression_invalidMixedCase_shouldThrowDescriptiveException() throws Exception {
        RunContext runContext = runContextFactory.of(Map.of("mode", "MarkdownV2"));

        EmbeddedServer embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        embeddedServer.start();

        String yaml = """
            endpointOverride: "%s"
            token: "token"
            channel: "channel"
            payload: "Hello"
            parseMode: "{{ mode }}"
            """.formatted(embeddedServer.getURL().toString());

        TelegramSend task = JacksonMapper.ofYaml().readValue(yaml, TelegramSend.class);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.run(runContext)
        );

        assertThat(
            exception.getMessage(),
            equalToObject("Invalid parseMode value 'MarkdownV2'. Valid values (case-sensitive): HTML, MARKDOWNV2")
        );
    }

    @Test
    void parseMode_fromString_validValues() {
        assertThat(TelegramSend.ParseMode.fromString("HTML"), equalToObject(TelegramSend.ParseMode.HTML));
        assertThat(TelegramSend.ParseMode.fromString("MARKDOWNV2"), equalToObject(TelegramSend.ParseMode.MARKDOWNV2));
        assertThat(TelegramSend.ParseMode.fromString(null), equalToObject(null));
    }

    @Test
    void parseMode_fromString_invalidMixedCase_shouldThrowDescriptiveException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TelegramSend.ParseMode.fromString("MarkdownV2")
        );

        assertThat(
            exception.getMessage(),
            equalToObject("Invalid parseMode value 'MarkdownV2'. Valid values (case-sensitive): HTML, MARKDOWNV2")
        );
    }

    @Test
    void parseMode_fromString_invalidValue_shouldThrowDescriptiveException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TelegramSend.ParseMode.fromString("INVALID")
        );

        assertThat(
            exception.getMessage(),
            equalToObject("Invalid parseMode value 'INVALID'. Valid values (case-sensitive): HTML, MARKDOWNV2")
        );
    }

    @Test
    void deserialize_parseMode_withInvalidMixedCase_shouldThrowDescriptiveException() {
        Exception exception = assertThrows(
            Exception.class,
            () -> JacksonMapper.ofYaml().readValue("\"MarkdownV2\"", TelegramSend.ParseMode.class)
        );

        assertThat(
            exception.getMessage(),
            containsString("Invalid parseMode value 'MarkdownV2'. Valid values (case-sensitive): HTML, MARKDOWNV2")
        );
    }

    @Test
    void deserialize_parseMode_withValidValues() throws Exception {
        TelegramSend.ParseMode html = JacksonMapper.ofYaml().readValue("\"HTML\"", TelegramSend.ParseMode.class);
        TelegramSend.ParseMode markdown = JacksonMapper.ofYaml().readValue("\"MARKDOWNV2\"", TelegramSend.ParseMode.class);

        assertThat(html, equalToObject(TelegramSend.ParseMode.HTML));
        assertThat(markdown, equalToObject(TelegramSend.ParseMode.MARKDOWNV2));
    }
}
