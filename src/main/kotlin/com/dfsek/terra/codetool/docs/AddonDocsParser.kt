package com.dfsek.terra.codetool.docs

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.decodeFromString

class AddonDocsParser(private val project: Project) {
    private val cache = ConcurrentHashMap<String, AddonDocumentation>()
    
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false, allowAnchorsAndAliases = true))
    
    @Serializable
    data class AddonDocumentation(
        val objects: Map<String, ObjectDefinition> = emptyMap(),
        val templates: Map<String, Map<String, TemplateDefinition>> = emptyMap()
    )
    
    @Serializable
    data class ObjectDefinition(
        val type: String, val description: String? = null, val types: Map<String, TypeDefinition>? = null
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
        val type: String, val default: String? = null, val description: String? = null, val required: Boolean? = null
    )
    
    @Serializable
    private data class RawAddonFile(
        val root: Map<String, RawAddonContent> = emptyMap()
    )
    
    @Serializable
    private data class RawAddonContent(
        val objects: Map<String, JsonElement>? = null, val templates: Map<String, JsonElement>? = null
    )
    
    /**
     * Parse all addon documentation files in the project
     */
    fun parseAllAddonDocs(): Map<String, AddonDocumentation> {
        return ReadAction.compute<Map<String, AddonDocumentation>, RuntimeException> {
            val result = mutableMapOf<String, AddonDocumentation>()
            
            // Find all YAML files in the project
            val yamlFiles = FileTypeIndex.getFiles(
                YAMLFileType.YML, GlobalSearchScope.allScope(project)
            )
            
            val psiManager = PsiManager.getInstance(project)
            
            yamlFiles.forEach { virtualFile ->
                // Check if this is an addon documentation file
                if (isAddonDocFile(virtualFile)) {
                    val psiFile = psiManager.findFile(virtualFile) as? YAMLFile
                    psiFile?.let { yamlFile ->
                        try {
                            val addonName = extractAddonName(virtualFile.name)
                            val documentation = parseAddonDocumentation(yamlFile)
                            if (documentation != null) {
                                result[addonName] = documentation
                                cache[addonName] = documentation
                            }
                        } catch (e: Exception) {
                            // Log error but continue processing other files
                            println("Error parsing addon doc file ${virtualFile.name}: ${e.message}")
                        }
                    }
                }
            }
            
            result
        }
    }
    
    /**
     * Parse a single addon documentation YAML file
     */
    fun parseAddonDocumentation(yamlFile: YAMLFile): AddonDocumentation? {
        return try {
            val yamlText = yamlFile.text
            val rawData = parseRawYamlData(yamlText)
            
            // Extract the first (and typically only) addon from the file
            val addonContent = rawData.values.firstOrNull() ?: return null
            
            val objects = parseObjectsFromJson(addonContent.objects)
            val templates = parseTemplatesFromJson(addonContent.templates)
            
            AddonDocumentation(objects, templates)
        } catch (e: Exception) {
            println("Failed to parse YAML file: ${e.message}")
            null
        }
    }
    
    /**
     * Parse raw YAML data into a map structure
     */
    private fun parseRawYamlData(yamlText: String): Map<String, RawAddonContent> {
        return try {
            // Parse as a generic map first to handle dynamic keys
            val genericMap = yaml.decodeFromString<Map<String, JsonElement>>(yamlText)
            
            val result = mutableMapOf<String, RawAddonContent>()
            
            genericMap.forEach { (addonName, addonElement) ->
                if (addonElement is JsonObject) {
                    val objects = addonElement["objects"] as? JsonObject
                    val templates = addonElement["templates"] as? JsonObject
                    
                    result[addonName] = RawAddonContent(
                        objects = objects?.let { mapOf("objects" to it) },
                        templates = templates?.let { mapOf("templates" to it) })
                }
            }
            
            result
        } catch (e: Exception) {
            println("Error parsing raw YAML data: ${e.message}")
            emptyMap()
        }
    }
    
    /**
     * Parse objects from JsonElement
     */
    private fun parseObjectsFromJson(objectsMap: Map<String, JsonElement>?): Map<String, ObjectDefinition> {
        if (objectsMap == null) return emptyMap()
        
        val objectsElement = objectsMap["objects"] as? JsonObject ?: return emptyMap()
        
        val result = mutableMapOf<String, ObjectDefinition>()
        
        objectsElement.forEach { (objectName, objectElement) ->
            if (objectElement is JsonObject) {
                val type = (objectElement["type"] as? JsonPrimitive)?.content ?: "UNKNOWN"
                val description = (objectElement["description"] as? JsonPrimitive)?.content
                val types = parseTypesFromJson(objectElement["types"] as? JsonObject)
                
                result[objectName] = ObjectDefinition(type, description, types)
            }
        }
        
        return result
    }
    
    /**
     * Parse types from JsonObject
     */
    private fun parseTypesFromJson(typesObject: JsonObject?): Map<String, TypeDefinition>? {
        if (typesObject == null) return null
        
        val result = mutableMapOf<String, TypeDefinition>()
        
        typesObject.forEach { (typeName, typeElement) ->
            val description = if (typeElement is JsonObject) {
                (typeElement["description"] as? JsonPrimitive)?.content
            } else null
            
            result[typeName] = TypeDefinition(description)
        }
        
        return result
    }
    
    /**
     * Parse templates from JsonElement
     */
    private fun parseTemplatesFromJson(templatesMap: Map<String, JsonElement>?): Map<String, Map<String, TemplateDefinition>> {
        if (templatesMap == null) return emptyMap()
        
        val templatesElement = templatesMap["templates"] as? JsonObject ?: return emptyMap()
        
        val result = mutableMapOf<String, Map<String, TemplateDefinition>>()
        
        templatesElement.forEach { (categoryName, categoryElement) ->
            if (categoryElement is JsonObject) {
                val categoryTemplates = mutableMapOf<String, TemplateDefinition>()
                
                categoryElement.forEach { (templateName, templateElement) ->
                    if (templateElement is JsonObject) {
                        val isAbstract = (templateElement["abstract"] as? JsonPrimitive)?.content?.toBoolean()
                        val extendsTemplate = (templateElement["extends"] as? JsonPrimitive)?.content
                        val description = (templateElement["description"] as? JsonPrimitive)?.content
                        val params = parseParametersFromJson(templateElement["params"] as? JsonObject)
                        
                        categoryTemplates[templateName] = TemplateDefinition(
                            isAbstract = isAbstract, extendsTemplate = extendsTemplate, params = params, description = description
                        )
                    }
                }
                
                result[categoryName] = categoryTemplates
            }
        }
        
        return result
    }
    
    /**
     * Parse parameters from JsonObject
     */
    private fun parseParametersFromJson(paramsObject: JsonObject?): Map<String, ParameterDefinition> {
        if (paramsObject == null) return emptyMap()
        
        val result = mutableMapOf<String, ParameterDefinition>()
        
        paramsObject.forEach { (paramName, paramElement) ->
            if (paramElement is JsonObject) {
                val type = (paramElement["type"] as? JsonPrimitive)?.content ?: "Unknown"
                val default = (paramElement["default"] as? JsonPrimitive)?.content
                val description = (paramElement["description"] as? JsonPrimitive)?.content
                val required = (paramElement["required"] as? JsonPrimitive)?.content?.toBoolean()
                
                result[paramName] = ParameterDefinition(type, default, description, required)
            }
        }
        
        return result
    }
    
    /**
     * Check if a file is an addon documentation file
     */
    private fun isAddonDocFile(virtualFile: VirtualFile): Boolean {
        val name = virtualFile.name
        return name.endsWith(".yml") || name.endsWith(".yaml")
    }
    
    /**
     * Extract addon name from filename
     */
    private fun extractAddonName(filename: String): String {
        return filename.substringBeforeLast('.').replace('-', '_')
    }
    
    /**
     * Get documentation for a specific addon
     */
    fun getAddonDocumentation(addonName: String): AddonDocumentation? {
        return cache[addonName] ?: run {
            // Try to reload if not in cache
            parseAllAddonDocs()
            cache[addonName]
        }
    }
    
    /**
     * Get all available object types across all addons
     */
    fun getAllObjectTypes(): Set<String> {
        val allDocs = if (cache.isEmpty()) parseAllAddonDocs() else cache
        return allDocs.values.flatMap { it.objects.keys }.toSet()
    }
    
    /**
     * Get all available template types for a category
     */
    fun getTemplateTypes(category: String): Set<String> {
        val allDocs = if (cache.isEmpty()) parseAllAddonDocs() else cache
        return allDocs.values.flatMap { it.templates[category]?.keys ?: emptySet() }.toSet()
    }
    
    /**
     * Find object definition by name across all addons
     */
    fun findObjectDefinition(objectName: String): ObjectDefinition? {
        val allDocs = if (cache.isEmpty()) parseAllAddonDocs() else cache
        return allDocs.values.firstNotNullOfOrNull { it.objects[objectName] }
    }
    
    /**
     * Find template definition by category and name
     */
    fun findTemplateDefinition(category: String, templateName: String): TemplateDefinition? {
        val allDocs = if (cache.isEmpty()) parseAllAddonDocs() else cache
        return allDocs.values.firstNotNullOfOrNull { it.templates[category]?.get(templateName) }
    }
    
    /**
     * Get parameter suggestions for a template
     */
    fun getParameterSuggestions(category: String, templateName: String): Map<String, ParameterDefinition> {
        val template = findTemplateDefinition(category, templateName) ?: return emptyMap()
        val result = template.params.toMutableMap()
        
        // If template extends another, merge parent parameters
        template.extendsTemplate?.let { parentName ->
            val parentTemplate = findTemplateDefinition(category, parentName)
            parentTemplate?.params?.forEach { (key, value) ->
                if (!result.containsKey(key)) {
                    result[key] = value
                }
            }
        }
        
        return result
    }
    
    /**
     * Clear the cache (useful for testing or when files change)
     */
    fun clearCache() {
        cache.clear()
    }
}