package com.dfsek.terra.codetool.envman.service

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import java.io.File

@State(
    name = "TerraEnvironmentRegistry",
    storages = [Storage("terra_environments.xml")]
)
@Service(Service.Level.APP)
class TerraEnvironmentService : PersistentStateComponent<TerraEnvironmentState> {
    private val configPath = File(PathManager.getConfigPath() + "/terra-codetool/environments").also { it.mkdirs() }
    private var state = TerraEnvironmentState()
    
    fun getEnvironments() = state.environments
    
    fun createEnvironment() {
    
    }
    
    override fun getState(): TerraEnvironmentState = state
    
    override fun loadState(state: TerraEnvironmentState) {
        this.state = state
    }
    
    companion object {
        fun getInstance(): TerraEnvironmentService = service()
    }
}