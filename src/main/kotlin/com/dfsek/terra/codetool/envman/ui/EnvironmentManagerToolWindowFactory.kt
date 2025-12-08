package com.dfsek.terra.codetool.envman.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class EnvironmentManagerToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = EnvironmentConfigPanel(project)
        val factory = ContentFactory.getInstance()
        val toolWindowContent = factory.createContent(content, "", false)
        toolWindow.contentManager.addContent(toolWindowContent)
    }
}