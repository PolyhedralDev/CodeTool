package com.dfsek.terra.codetool.envman.service.version

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Service(Service.Level.APP)
class JenkinsVersionExplorer : TerraVersionExplorer {
    private val httpClient = HttpClient()
    private val baseUrl = "https://ci.codemc.io/job/PolyhedralDev/job/Terra/"
    private val apiUrl = "$baseUrl/api/json?tree=builds[number,timestamp,result,artifacts[fileName,relativePath]]"
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Serializable
    data class JenkinsArtifact(
        val fileName: String,
        val relativePath: String
    )
    
    @Serializable
    data class JenkinsBuild(
        val number: Int,
        val timestamp: Long,
        val result: String?,
        val artifacts: List<JenkinsArtifact>
    )
    
    @Serializable
    data class JenkinsJobResponse(
        val builds: List<JenkinsBuild>
    )
    
    override suspend fun getVersions(): List<TerraVersion> = try {
        val response: JenkinsJobResponse = httpClient.get(apiUrl)
            .let { json.decodeFromString(it.body()) }
        
        response.builds
            .filter { it.result == "SUCCESS" }
            .sortedByDescending { it.timestamp }
            .mapNotNull { build ->
                val artifact = build.artifacts.firstOrNull {
                    it.fileName.contains("fabric")
                } ?: return@mapNotNull null
                
                val guessedVersion = artifact.fileName.substringAfterLast("/").removePrefix("Terra-fabric-").removeSuffix(".jar")
                
                val downloadUrl = "$baseUrl/${build.number}/artifact/${artifact.relativePath}"
                TerraVersion(
                    version = "Build #${build.number} - $guessedVersion",
                    downloadUrl = downloadUrl
                )
            }
    } catch (e: Exception) {
        println("Failed to fetch Jenkins versions: ${e.message}")
        emptyList()
    }
    
    companion object {
        fun getInstance(): JenkinsVersionExplorer = service()
    }
}