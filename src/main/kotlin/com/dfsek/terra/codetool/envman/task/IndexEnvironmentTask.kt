package com.dfsek.terra.codetool.envman.task

import com.dfsek.terra.codetool.envman.service.TerraEnvironmentService
import com.dfsek.terra.codetool.envman.service.state.EnvironmentTasks
import com.dfsek.terra.codetool.envman.service.state.TerraEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class IndexEnvironmentTask(
    project: Project,
    val onCompleted: () -> Unit
) : Task.Backgroundable(project, "Indexing environments...", true) {
    val envService = TerraEnvironmentService.getInstance()
    val logger = Logger.getInstance(IndexEnvironmentTask::class.java)
    
    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = false
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
        val tasks = EnvironmentTasks.entries.filterNot { it in environment.tasksDone }
        logger.info("Indexing environment ${environment.name} with tasks ${tasks.joinToString { it.name }}")
        tasks.map { task ->
            task.snippet.apply(environment)
            environment.tasksDone.add(task)
        }
    }
}