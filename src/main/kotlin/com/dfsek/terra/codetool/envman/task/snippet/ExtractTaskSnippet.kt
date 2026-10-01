package com.dfsek.terra.codetool.envman.task.snippet

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentService
import com.dfsek.terra.codetool.envman.service.state.TerraEnvironment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.ZipUtil
import java.io.File
import java.util.zip.ZipFile
import kotlinx.serialization.decodeFromString

object ExtractTaskSnippet : TaskSnippet {
    private val envService by lazy { TerraEnvironmentService.getInstance() }
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
    private val logger = Logger.getInstance(ExtractTaskSnippet::class.java)
    
    override fun apply(environment: TerraEnvironment) {
        val parent = envService.getParentDirectory(environment)
        val terraJar = parent.resolve("terra.jar")
        val terraJarZip = ZipFile(terraJar)
        val resourcesYmlEntry = terraJarZip.getEntry("resources.yml")!!
        val resourcesYmlText = terraJarZip.getInputStream(resourcesYmlEntry).bufferedReader().use { reader -> reader.readText() }
        val paths = yaml.decodeFromString<Map<String, List<String>>>(resourcesYmlText)
            .flatMap { (key, value) -> value.map { "$key/$it" } }
        logger.info("Extracting ${paths.size} files from terra.jar...")
        ZipUtil.extract(terraJar.toPath(), parent.toPath()) { path, name ->
            File(path, name).relativeTo(parent).toString() in paths
        }
        logger.info("Done")
    }
}