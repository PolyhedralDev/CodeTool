package com.dfsek.terra.codetool.envman.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class EnvironmentCreationService(val project: Project) {
    val environmentService = TerraEnvironmentService.getInstance()
    
    fun createEnvironment(
        name: String,
        then: () -> Unit
    ) {
    
    }
    
    companion object {
        fun getInstance(project: Project): EnvironmentCreationService = project.service()
    }
}