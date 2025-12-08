package com.dfsek.terra.codetool.envman.task

import com.dfsek.terra.codetool.envman.service.TerraEnvironment
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class IndexEnvironmentTask(
    project: Project,
    val onCompleted: () -> Unit
) : Task.Backgroundable(project, "Indexing environments...", true) {
    val envService = TerraEnvironmentService.Companion.getInstance()
    
    override fun run(indicator: ProgressIndicator) {
        val environments = envService.getEnvironments()
        
        for ((i, environment) in environments.withIndex()) {
            indicator.checkCanceled()
            
            indicator.text = "Indexing environment ${environment.name}"
            indicator.fraction = i / environments.size.toDouble()
            
            performIndexing(environment)
        }
        
        ApplicationManager.getApplication().invokeLater {
            onCompleted()
        }
    }
    
    fun performIndexing(environment: TerraEnvironment) {
        // TODO
    }
}