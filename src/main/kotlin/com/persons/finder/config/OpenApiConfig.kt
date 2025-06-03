package com.persons.finder.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenApiCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi: OpenAPI ->
            if (openApi.components == null) {
                openApi.components = Components()
            }
            if (openApi.components.schemas == null) {
                openApi.components.schemas = HashMap()
            }

            // Define the ValidationErrorResponse schema for swagger-ui doc
            val validationErrorSchema = ObjectSchema()
                .addProperty("timestamp", StringSchema().format("date-time").example("2023-10-26T10:00:00"))
                .addProperty("status", IntegerSchema().format("int32").example(400))
                .addProperty("message", StringSchema().example("Validation failed"))
                .addProperty(
                    "errors", ArraySchema().items(
                        ObjectSchema()
                            .addProperty("field", StringSchema().example("latitude"))
                            .addProperty("defaultMessage", StringSchema().example("must not be null"))
                    )
                )
                .description("Detailed response for validation errors")

            openApi.components.addSchemas("ValidationErrorResponse", validationErrorSchema)
        }
    }
}