package com.dfsek.terra.codetool.envman.ui

import com.dfsek.terra.codetool.TerraIcons
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentChangeListener
import com.dfsek.terra.codetool.envman.service.TerraEnvironmentService
import com.dfsek.terra.codetool.envman.ui.create.AddEnvironmentDialog
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel

class EnvironmentConfigPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val registry = TerraEnvironmentService.getInstance()
    private val serviceController = EnvironmentServiceController(project)
    private val environmentList = JBList<String>(DefaultListModel()).apply {
        emptyText.text = "No environments"
        border = JBUI.Borders.empty()
        cellRenderer = EnvironmentCellRenderer
    }
    
    init {
        val splitter = OnePixelSplitter(false, 0.25f)
        
        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(JBScrollPane(environmentList).apply {
            border = JBUI.Borders.empty()
        }, BorderLayout.CENTER)
        splitter.firstComponent = leftPanel
        
        val actionGroup = DefaultActionGroup().apply {
            add(object : AnAction("Add Environment", null, AllIcons.General.Add) {
                override fun actionPerformed(e: AnActionEvent) {
                    val project = e.project ?: return
                    val dialog = AddEnvironmentDialog(project)
                    if (dialog.showAndGet()) {
                        serviceController.triggerIndexing()
                    }
                }
            })
            add(object : AnAction("Remove Environment", null, AllIcons.General.Remove) {
                override fun actionPerformed(e: AnActionEvent) { /* Remove logic */ }
            })
        }
        
        val toolbar = ActionManager.getInstance().createActionToolbar("TerraEnvToolbar", actionGroup, true)
        toolbar.targetComponent = leftPanel
        toolbar.component.border = JBUI.Borders.empty(4)
        leftPanel.add(toolbar.component, BorderLayout.NORTH)
        
        splitter.secondComponent = AddonListEmptyState()
        
        add(splitter, BorderLayout.CENTER)
        border = JBUI.Borders.empty()
        
        refreshEnvironmentList()
        
        val busConnection = com.intellij.openapi.application.ApplicationManager.getApplication().messageBus.connect()
        busConnection.subscribe(TerraEnvironmentChangeListener.TOPIC, object : TerraEnvironmentChangeListener {
            override fun environmentChanged() {
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    refreshEnvironmentList()
                }
            }
        })
        
        environmentList.addListSelectionListener {
            updateDetailPane(splitter)
        }
    }
    
    private fun refreshEnvironmentList() {
        val model = environmentList.model as DefaultListModel<String>
        
        model.clear()
        registry.getEnvironments().forEach { env ->
            model.addElement(env.name)
        }
    }
    
    private fun updateDetailPane(splitter: OnePixelSplitter) {
        val selectedName = environmentList.selectedValue
        if (selectedName == null) {
            splitter.secondComponent = AddonListEmptyState()
        } else {
            val env = registry.getEnvironments().find { it.name == selectedName } ?: return
            splitter.secondComponent = AddonList(env)
        }
        splitter.revalidate()
        splitter.repaint()
    }
    
    object EnvironmentCellRenderer : ColoredListCellRenderer<String>() {
        override fun customizeCellRenderer(
            list: JList<out String?>,
            value: String?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            icon = TerraIcons.TerraSimple
            value?.let { append(it, SimpleTextAttributes.REGULAR_ATTRIBUTES) }
        }
    
        private fun readResolve(): Any = EnvironmentCellRenderer
    }
}