package org.integritylens.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "IntegrityLens API", version = "0.1.0",
        description = "MVP. JPlag и structsim возвращают детерминированные mock-оценки."),
        servers = @Server(url = "/"))
public class OpenApiConfiguration {
    @Bean
    OpenApiCustomizer nullableSchemas() {
        return api -> api.getComponents().getSchemas().values().forEach(OpenApiConfiguration::normalizeNullable);
    }

    private static <T> void normalizeNullable(Schema<T> schema) {
        if (schema.getTypes() != null && schema.getTypes().contains("null")) {
            if (schema.getEnum() != null && !schema.getEnum().contains(null)) {
                var values = new ArrayList<>(schema.getEnum());
                values.add(null);
                schema.setEnum(values);
            }
            // A nullable $ref is a union in OpenAPI 3.1, not a conjunction with type: null.
            if (schema.get$ref() != null) {
                String reference = schema.get$ref();
                schema.set$ref(null);
                schema.setTypes(null);
                schema.addAnyOfItem(new Schema<>().$ref(reference));
                schema.addAnyOfItem(new Schema<>().types(Set.of("null")));
            }
        }
        if (schema.getProperties() != null) schema.getProperties().values().forEach(OpenApiConfiguration::normalizeNullable);
        if (schema.getItems() != null) normalizeNullable(schema.getItems());
        if (schema.getAdditionalProperties() instanceof Schema<?> value) normalizeNullable(value);
    }
}
