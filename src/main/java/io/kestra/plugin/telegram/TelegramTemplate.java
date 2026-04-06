package io.kestra.plugin.telegram;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import io.kestra.core.models.annotations.PluginProperty;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class TelegramTemplate extends TelegramSend {

    @Schema(
        title = "Template resource path",
        description = "Classpath Pebble template used to build the message payload before sending.",
        hidden = true
    )
    @PluginProperty(group = "advanced")
    protected Property<String> templateUri;

    @Schema(
        title = "Template variables",
        description = "Map of variables rendered and passed to the Pebble template; defaults to an empty map when not provided."
    )
    @PluginProperty(group = "advanced")
    protected Property<Map<String, Object>> templateRenderMap;

    @SuppressWarnings("unchecked")
    @Override
    public VoidOutput run(RunContext runContext) throws Exception {

        final var renderedTemplateUri = runContext.render(this.templateUri).as(String.class);
        if (renderedTemplateUri.isPresent()) {
            String template = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(renderedTemplateUri.get())),
                StandardCharsets.UTF_8
            );

            this.payload = Property.ofValue(
                runContext.render(
                    template, templateRenderMap != null ? runContext.render(templateRenderMap).asMap(String.class, Object.class) : Map.of()
                )
            );
        }

        return super.run(runContext);
    }
}
