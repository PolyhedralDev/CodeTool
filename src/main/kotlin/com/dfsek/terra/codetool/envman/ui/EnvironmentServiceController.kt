package com.dfsek.terra.codetool.envman.ui

import com.dfsek.terra.codetool.envman.service.TerraEnvironmentChangeListener
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentService
import com.dfsek.terra.codetool.envman.service.VersionDetectionService
import com.dfsek.terra.codetool.envman.task.IndexEnvironmentTask
import com.dfsek.terra.codetool.showTerraBalloon
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import java.io.File

class EnvironmentServiceController(val project: Project) {
    val terraEnvironmentService = TerraEnvironmentService.getInstance()
    val versionDetector = VersionDetectionService.getInstance()
    fun triggerIndexing() {
        ProgressManager.getInstance().run(IndexEnvironmentTask(project) {
            triggerUiReload()
        })
    }
    
    fun createEnvironment(
        project: Project,
        name: String,
        addJarFile: (String) -> Unit
    ) {
        val prepared = terraEnvironmentService.prepareEnvironmentCreation()
        addJarFile(prepared.path)
        val version = versionDetector.detectVersion(File(prepared.path, "terra.jar"))
        if (version == null) {
            showTerraBalloon(project, "Error", "Could not detect version of Terra", NotificationType.ERROR)
            terraEnvironmentService.cleanupPreparedEnvironment(prepared)
            return
        }
        
        terraEnvironmentService.promote(prepared, name, version)
        triggerIndexing()
    }
    
    private fun triggerUiReload() {
        ApplicationManager.getApplication().messageBus
            .syncPublisher(TerraEnvironmentChangeListener.TOPIC)
            .environmentChanged()
    }
}