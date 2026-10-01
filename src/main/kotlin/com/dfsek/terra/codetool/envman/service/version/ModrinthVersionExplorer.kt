package com.dfsek.terra.codetool.envman.service.version

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Service(Service.Level.APP)
class ModrinthVersionExplorer : TerraVersionExplorer {
    val httpClient = HttpClient()
    private val url = "https://api.modrinth.com/v2/project/terra/version?loaders=[%22fabric%22]"
    val json = Json {
        ignoreUnknownKeys = true
    }
    
    @Serializable
    data class ModrinthVersionFile(
        val url: String,
        val primary: Boolean
    )
    
    @Serializable
    data class ModrinthVersion(
        @SerialName("version_number") val versionNumber: String,
        @SerialName("date_published") val datePublished: String,
        val changelog: String,
        val files: List<ModrinthVersionFile>
    )
    
    override suspend fun getVersions(): List<TerraVersion> = try {
        httpClient.get(url)
            .let { json.decodeFromString<List<ModrinthVersion>>(it.body()) }
            .sortedByDescending { it.datePublished }
            .mapNotNull { version ->
                val file = version.files.firstOrNull { it.primary } ?: return@mapNotNull null
                TerraVersion(version.versionNumber, file.url)
            }
    } catch (_: Exception) {
        emptyList()
    }
    
    companion object {
        fun getInstance(): ModrinthVersionExplorer = service()
    }
}