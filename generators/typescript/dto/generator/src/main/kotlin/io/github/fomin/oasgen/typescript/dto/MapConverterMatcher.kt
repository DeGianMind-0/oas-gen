package io.github.fomin.oasgen.typescript.dto

import io.github.fomin.oasgen.JsonSchema
import io.github.fomin.oasgen.JsonType
import io.github.fomin.oasgen.java.toLowerCamelCase

class MapConverterMatcher : TypeConverterMatcher {
    class Provider : TypeConverterMatcherProvider {
        override val id = "map"
        override fun provide() = MapConverterMatcher()
    }

    override fun match(typeConverterRegistry: TypeConverterRegistry, jsonSchema: JsonSchema): TypeConverter? {
        val valueSchema = jsonSchema.additionalProperties()
        return if (jsonSchema.type == JsonType.OBJECT && valueSchema != null) {
            object : TypeConverter {
                val valueTypeConverter = typeConverterRegistry[valueSchema]
                val valueJsonConverter = valueTypeConverter.jsonConverter

                override fun type() = "Record<string, ${valueTypeConverter.type()}>"

                override fun content(): String? = null

                override fun importDeclarations() = emptyList<ImportDeclaration>()

                override fun innerSchemas() = listOf(valueSchema)

                override val jsonConverter = if (valueJsonConverter != null) {
                    object : JsonConverter {
                        override fun toJson(valueExpression: String) =
                            "mapObjectProperties(value, (_, v) => ${valueJsonConverter.toJson("v")})"

                        override fun fromJson(valueExpression: String) =
                            "mapObjectProperties(value, (_, v) => ${valueJsonConverter.fromJson("v")})"

                        override fun content(): String? = null

                    }
                } else {
                    null
                }

            }
        } else {
            null
        }
    }
}