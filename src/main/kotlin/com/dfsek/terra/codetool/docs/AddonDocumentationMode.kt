package com.dfsek.terra.codetool.docs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ObjectDefinition(
    val type: String,
    val description: String? = null,
    val types: Map<String, TypeDefinition>? = null
)

@Serializable
data class TypeDefinition(
    val description: String? = null
)

@Serializable
data class TemplateDefinition(
    @SerialName("abstract") val isAbstract: Boolean? = null,
    @SerialName("extends") val extendsTemplate: String? = null,
    val params: Map<String, ParameterDefinition> = emptyMap(),
    val description: String? = null
)

@Serializable
data class ParameterDefinition(
    val type: String,
    val default: String? = null,
    val description: String? = null,
    val required: Boolean? = null
)

@Serializable
private data class ConfigDocumentation(
    val description: String? = null,
    val useGlobalTemplate: Boolean = true,
)

@Serializable
private data class AddonFile(
    val root: Map<String, AddonContent> = emptyMap()
)

@Serializable
private data class AddonContent(
    val objects: Map<String, ObjectDefinition> = emptyMap(),
    val configs: Map<String, JsonElement> = emptyMap()
)