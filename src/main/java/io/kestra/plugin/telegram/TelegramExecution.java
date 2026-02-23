package io.kestra.plugin.telegram;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.plugins.notifications.ExecutionInterface;
import io.kestra.core.plugins.notifications.ExecutionService;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Notify Telegram about execution result",
    description = "Sends a templated Telegram message with execution link, identifiers, timing, status, and failing task when applicable. Use with a [Flow trigger](https://kestra.io/docs/administrator-guide/monitoring#alerting); for `errors` handlers prefer [TelegramSend](https://kestra.io/plugins/plugin-telegram/io.kestra.plugin.telegram.telegramsend)."
)
@Plugin(
    examples = {
        @Example(
            title = "Send a Telegram notification on a failed flow execution.",
            full = true,
            code = """
                id: failure_alert
                namespace: company.team

                tasks:
                  - id: send_alert
                    type: io.kestra.plugin.telegram.TelegramExecution
                    token: "{{ secret('TELEGRAM_TOKEN') }}" # format: 6090305634:xyz
                    channel: "2072728690"
                    executionId: "{{ trigger.executionId }}"

                triggers:
                  - id: failed_prod_workflows
                    type: io.kestra.plugin.core.trigger.Flow
                    conditions:
                      - type: io.kestra.plugin.core.condition.ExecutionStatus
                        in:
                          - FAILED
                          - WARNING
                      - type: io.kestra.plugin.core.condition.ExecutionNamespace
                        namespace: prod
                        prefix: true
                """
        )
    },
    aliases = "io.kestra.plugin.notifications.telegram.TelegramExecution"
)
public class TelegramExecution extends TelegramTemplate implements ExecutionInterface {

    @Builder.Default
    private final Property<String> executionId = Property.ofExpression("{{ execution.id }}");
    private Property<Map<String, Object>> customFields;
    private Property<String> customMessage;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        this.templateUri = Property.ofValue("telegram-template.peb");
        this.templateRenderMap = Property.ofValue(ExecutionService.executionMap(runContext, this));
        return super.run(runContext);
    }
}
