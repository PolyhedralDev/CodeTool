package com.dfsek.terra.codetool.project

import com.dfsek.terra.codetool.TerraIcons
import com.intellij.openapi.application.NonBlockingReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import com.intellij.util.messages.MessageBusConnection
import java.awt.Component
import java.awt.event.MouseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import kotlinx.coroutines.runBlocking

class ProjectStatusBarWidget(val project: Project, val scope: CoroutineScope) : StatusBarWidget, StatusBarWidget.Multiframe {
    private var connection: MessageBusConnection? = null
    private var lastRoot: ProjectRootUtility.ProjectRoot? = null
    override fun ID(): @NonNls String = "terra_project_name"
    
    override fun install(statusBar: StatusBar) {
        connection = project.messageBus.connect(this)
        connection?.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                update(statusBar)
            }
            
            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                update(statusBar)
            }
            
            override fun selectionChanged(event: FileEditorManagerEvent) {
                update(statusBar)
            }
        })
        connection?.subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun exitDumbMode() {
                update(statusBar)
            }
        })
        update(statusBar)
    }
    
    override fun dispose() {
        connection?.disconnect()
    }
    
    override fun getPresentation(): StatusBarWidget.WidgetPresentation? {
        return object : StatusBarWidget.IconPresentation {
            override fun getIcon() = TerraIcons.TerraSimple
            override fun getTooltipText(): String? {
                val id = lastRoot?.id
                val addons = lastRoot?.addons?.let { formatThreeElementsPlusAmount(it) }
                return when {
                    id != null && addons != null && addons != "Empty" -> "$id: $addons"
                    id != null -> id
                    else       -> "No Terra project found"
                }
            }
            override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
                FileEditorManager.getInstance(project)
                    .openFile(lastRoot?.root ?: return@Consumer, true)
            }
        }
    }
    
    private fun updateContent() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        val currentFile = fileEditorManager.selectedEditor?.file ?: run { lastRoot = null; return }
        lastRoot = ProjectRootUtility.getNearestRoot(currentFile, project)
    }
    
    private fun update(statusBar: StatusBar) = scope.launch {
        withContext(Dispatchers.IO) {
            smartReadAction(project) {
                updateContent()
            }
        }
        statusBar.updateWidget(ID())
    }
    
    override fun copy(): StatusBarWidget = ProjectStatusBarWidget(project, scope)
    
    private fun formatThreeElementsPlusAmount(elements: List<String>): String {
        if (elements.isEmpty()) return "Empty"
        if (elements.size < 4) return elements.joinToString(", ")
        return elements.take(3).joinToString(", ") + ", (+" + (elements.size - 3) + " more)"
    }
}