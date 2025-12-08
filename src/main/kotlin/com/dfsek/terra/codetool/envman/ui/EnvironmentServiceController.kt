package com.dfsek.terra.codetool.envman.ui

import com.dfsek.terra.codetool.envman.task.IndexEnvironmentTask
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentChangeListener
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project

class EnvironmentServiceController(val project: Project) {
    val terraEnvironmentService = TerraEnvironmentService.getInstance()
    
    fun triggerIndexing() {
        ProgressManager.getInstance().run(IndexEnvironmentTask(project) {
            triggerUiReload()
        })
    }
    
    fun createEnvironment() {
    
    }
    
    private fun triggerUiReload() {
        ApplicationManager.getApplication().messageBus
            .syncPublisher(TerraEnvironmentChangeListener.TOPIC)
            .environmentChanged()
    }
}