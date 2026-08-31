package io.kestra.plugin.telegram;

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
    void parseMode_fromString_validValues() {
        assertThat(TelegramSend.ParseMode.fromString("HTML"), equalToObject(TelegramSend.ParseMode.HTML));
        assertThat(TelegramSend.ParseMode.fromString("MARKDOWNV2"), equalToObject(TelegramSend.ParseMode.MARKDOWNV2));
        assertThat(TelegramSend.ParseMode.fromString(null), equalToObject(null));
    }

    @Test
    void parseMode_fromString_invalidMixedCase_shouldThrowDescriptiveException() {
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
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
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
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
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
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
