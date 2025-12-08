package com.dfsek.terra.codetool.envman.task

import com.dfsek.terra.codetool.envman.ui.EnvironmentServiceController
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class EnvironmentCreationTask(
    val parentProject: Project,
    val controller: EnvironmentServiceController,
    val name: String,
    val addJarFile: (String) -> Unit
) : Task.Backgroundable(parentProject, "Creating environment...") {
    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        controller.createEnvironment(parentProject, name, addJarFile)
    }
}