package com.dfsek.terra.codetool.envman.ui.create

import com.dfsek.terra.codetool.envman.service.version.TerraVersion
import com.dfsek.terra.codetool.envman.service.version.TerraVersionExplorer
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.GridBagLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.SwingConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RemotePanel(
    private val type: AddEnvironmentDialog.SidebarRenderer.CellItem,
    private val explorer: TerraVersionExplorer,
    private val onChange: () -> Unit
) : JPanel(BorderLayout()) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val cardLayout = CardLayout()
    private val root = JPanel(cardLayout)
    
    private val listModel = DefaultListModel<TerraVersion>()
    private val versionList = JBList(listModel).apply {
        cellRenderer = object : ColoredListCellRenderer<TerraVersion>() {
        override fun customizeCellRenderer(
            list: JList<out TerraVersion>,
            value: TerraVersion?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            icon = AllIcons.Nodes.Artifact
            value?.let { append(it.version, SimpleTextAttributes.REGULAR_ATTRIBUTES) }
        }
    }
    }
    
    init {
        border = JBUI.Borders.empty(20)
        
        root.add(createLoadingPanel(), "LOADING")
        root.add(createContentPanel(), "CONTENT")
        
        add(root, BorderLayout.CENTER)
        cardLayout.show(root, "LOADING")
        
        versionList.addListSelectionListener { onChange() }
        
        fetchVersions()
    }
    
    private fun fetchVersions() {
        scope.launch {
            val versions = explorer.getVersions()
            ApplicationManager.getApplication().invokeLater {
                if (versions.isEmpty()) {
                    root.add(JBLabel("No versions available"), "CONTENT")
                } else {
                    versions.forEach { listModel.addElement(it) }
                    versionList.selectedIndex = 0
                }
                cardLayout.show(root, "CONTENT")
            }
        }
    }
    
    private fun createLoadingPanel() = JPanel(GridBagLayout()).apply {
        add(JBLabel("Fetching versions for ${type.label}...").apply {
            foreground = UIUtil.getContextHelpForeground()
            horizontalAlignment = SwingConstants.CENTER
        })
    }
    
    private fun createContentPanel() = JPanel(BorderLayout(0, 10)).apply {
        val title = JBLabel("Available ${type.label} Builds").apply {
            font = font.deriveFont(java.awt.Font.BOLD)
        }
        
        add(title, BorderLayout.NORTH)
        add(JBScrollPane(versionList).apply {
            border = JBUI.Borders.customLine(com.intellij.ui.JBColor.border())
        }, BorderLayout.CENTER)
    }
    
    fun getSelectedVersion(): TerraVersion? = versionList.selectedValue
}