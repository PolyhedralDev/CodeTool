package com.dfsek.terra.codetool.envman.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import java.io.File
import java.util.zip.ZipFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Service(Service.Level.APP)
class VersionDetectionService {
    val json = Json {
        ignoreUnknownKeys = true
    }
    
    private fun getModJson(file: File): String? {
        return try {
            ZipFile(file).use { zip ->
                val entry = zip.getEntry("fabric.mod.json") ?: return null
                zip.getInputStream(entry).use { it.readBytes().toString(Charsets.UTF_8) }
            }
        } catch (_: Exception) {
            null
        }
    }
    
    fun detectVersion(file: File): String? {
        val modJson = getModJson(file) ?: return null
        val loaded = json.decodeFromString<ModJson>(modJson)
        if (loaded.id != "terra") return null
        return loaded.version
    }
    
    @Serializable
    data class ModJson(val id: String, val version: String)
    
    companion object {
        fun getInstance() = service<VersionDetectionService>()
    }
}