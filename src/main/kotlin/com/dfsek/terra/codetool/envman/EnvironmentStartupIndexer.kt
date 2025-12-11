package com.dfsek.terra.codetool.envman

import com.dfsek.terra.codetool.envman.ui.EnvironmentServiceController
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class EnvironmentStartupIndexer : ProjectActivity {
    val logger = Logger.getInstance(EnvironmentStartupIndexer::class.java)
    override suspend fun execute(project: Project) {
        logger.info("Starting environment indexing...")
        val controller = EnvironmentServiceController(project)
        controller.triggerIndexing()
    }
}