package io.kestra.plugin.telegram;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.VariableRenderer;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * Renders telegram-template.peb directly. The rendered template, quotes included, is sent as the message
 * text, which Telegram limits to 4096 characters.
 */
@KestraTest
class TelegramTemplateCustomFieldsTest {
    private static final String BASE = "\"<company.team flow exec-id ➛ FAILED - Final task ID ➛ failed> Failed on task `failed` after PT1S (https://kestra.example/link)";

    @Inject
    private VariableRenderer variableRenderer;

    @Test
    void withoutCustomMessageOrFields_messageIsUnchanged() throws Exception {
        assertThat(render(Map.of()), is(BASE + "\""));
    }

    @Test
    void emptyCustomMessage_andNullFieldValue() throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("owner", null);

        assertThat(render(Map.of("customMessage", "", "customFields", fields)), is(BASE + "\n\nowner: -\""));
    }

    @Test
    void longCustomContent_staysWithinTelegramMessageLimit() throws Exception {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (int i = 1; i <= 50; i++) {
            fields.put("field" + i, "v".repeat(500));
        }

        var text = render(Map.of("customMessage", "m".repeat(5000), "customFields", fields));

        assertThat(text.length(), lessThanOrEqualTo(4096));
        assertThat(text, endsWith("...\""));
    }

    private String render(Map<String, Object> custom) throws Exception {
        var template = new String(
            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("telegram-template.peb")).readAllBytes(),
            StandardCharsets.UTF_8
        );
        Map<String, Object> variables = new HashMap<>(Map.of(
            "execution", Map.of("id", "exec-id", "namespace", "company.team", "flowId", "flow", "state", Map.of("current", "FAILED")),
            "lastTask", Map.of("taskId", "failed"),
            "firstFailed", Map.of("taskId", "failed"),
            "duration", "PT1S",
            "link", "https://kestra.example/link"
        ));
        variables.putAll(custom);
        return variableRenderer.render(template, variables);
    }
}
