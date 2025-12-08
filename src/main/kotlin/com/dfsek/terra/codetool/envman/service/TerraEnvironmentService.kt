package com.dfsek.terra.codetool.envman.service

import com.dfsek.terra.codetool.envman.service.state.TerraEnvironment
import com.dfsek.terra.codetool.envman.service.state.TerraEnvironmentState
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import java.io.File
import java.util.UUID

@State(
    name = "TerraEnvironmentRegistry",
    storages = [Storage("terra_environments.xml")]
)
@Service(Service.Level.APP)
class TerraEnvironmentService : PersistentStateComponent<TerraEnvironmentState> {
    private val configPath = File(PathManager.getConfigPath() + "/terra-codetool/environments").also { it.mkdirs() }
    private var state = TerraEnvironmentState()
    
    fun getEnvironments() = state.environments
    
    fun prepareEnvironmentCreation(): PreparedEnvironment {
        val uuid = UUID.randomUUID().toString()
        val path = File(configPath, uuid).also { it.mkdirs() }.path
        return PreparedEnvironment(path, uuid)
    }
    
    fun cleanupPreparedEnvironment(prepared: PreparedEnvironment) {
        val file = File(prepared.path)
        if (file.exists()) file.deleteRecursively()
    }
    
    fun promote(prepared: PreparedEnvironment, name: String, version: String) {
        val environment = TerraEnvironment(UUID.randomUUID().toString(), name, prepared.path, version)
        state.environments.add(environment)
    }
    
    override fun getState(): TerraEnvironmentState = state
    
    override fun loadState(state: TerraEnvironmentState) {
        this.state = state
    }
    
    companion object {
        fun getInstance(): TerraEnvironmentService = service()
    }
}