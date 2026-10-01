package com.dfsek.terra.codetool.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.NonNls

class ProjectStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): @NonNls String = "terra_project"
    
    override fun getDisplayName(): @NlsContexts.ConfigurableName String = "Terra Project"
    
    override fun createWidget(project: Project, scope: CoroutineScope): StatusBarWidget {
        return ProjectStatusBarWidget(project, scope)
    }
}