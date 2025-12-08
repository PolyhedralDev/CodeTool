package com.dfsek.terra.codetool.envman.ui.create

import com.dfsek.terra.codetool.envman.service.download.JenkinsVersionExplorer
import com.dfsek.terra.codetool.envman.service.download.ModrinthVersionExplorer
import com.dfsek.terra.codetool.envman.service.download.TerraVersionExplorer
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Component
import javax.swing.DefaultListModel
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class AddEnvironmentDialog(val project: Project) : DialogWrapper(project) {
    private val sidebarModel = DefaultListModel<SidebarRenderer.CellItem>().apply {
        addElement(SidebarRenderer.CellItem.LOCAL)
        addElement(SidebarRenderer.CellItem.JENKINS)
        addElement(SidebarRenderer.CellItem.MODRINTH)
    }
    private val sidebar = JBList(sidebarModel)
    
    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)
    
    private val nameField = JBTextField().apply {
        emptyText.text = "Environment Name (e.g., Terra Snapshot 1.18)"
    }
    
    private val localPanel = LocalPanel(project) { initValidation() }
    private val jenkinsPanel = RemotePanel(SidebarRenderer.CellItem.JENKINS, JenkinsVersionExplorer.getInstance()) { initValidation() }
    private val modrinthPanel = RemotePanel(SidebarRenderer.CellItem.MODRINTH, ModrinthVersionExplorer.getInstance()) { initValidation() }
    
    init {
        title = "New Terra Environment"
        init()
    }
    
    override fun createNorthPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 10))
        panel.border = JBUI.Borders.empty(10, 10, 0, 10)
        panel.add(JBLabel("Name:"), BorderLayout.WEST)
        panel.add(nameField, BorderLayout.CENTER)
        
        nameField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = initValidation()
        })
        return panel
    }
    
    override fun createCenterPanel(): JComponent {
        val splitter = OnePixelSplitter(false, 0.25f)
        splitter.border = JBUI.Borders.customLine(JBColor.border())
        
        contentPanel.add(localPanel, SidebarRenderer.CellItem.LOCAL.label)
        contentPanel.add(jenkinsPanel, SidebarRenderer.CellItem.JENKINS.label)
        contentPanel.add(modrinthPanel, SidebarRenderer.CellItem.MODRINTH.label)
        
        sidebar.cellRenderer = SidebarRenderer
        
        sidebar.addListSelectionListener {
            val selected = sidebar.selectedValue ?: return@addListSelectionListener
            cardLayout.show(contentPanel, selected.label)
        }
        sidebar.selectedIndex = 0
        
        splitter.firstComponent = sidebar
        splitter.secondComponent = contentPanel
        
        splitter.setHonorComponentsMinimumSize(true)
        return splitter
    }
    
    override fun doValidate(): ValidationInfo? {
        if (nameField.text.isBlank()) {
            return ValidationInfo("Please enter a name for the environment", nameField)
        }
        
        val selected = sidebar.selectedValue ?: return null
        return when (selected) {
            SidebarRenderer.CellItem.LOCAL -> {
                if (localPanel.getSelectedPath()?.isNotEmpty() != true) {
                    ValidationInfo("Please select a valid local JAR file", localPanel)
                } else null
            }
            SidebarRenderer.CellItem.JENKINS -> {
                if (jenkinsPanel.getSelectedVersion() == null) {
                    ValidationInfo("Please select a build version", jenkinsPanel)
                } else null
            }
            SidebarRenderer.CellItem.MODRINTH -> {
                if (modrinthPanel.getSelectedVersion() == null) {
                    ValidationInfo("Please select a release version", modrinthPanel)
                } else null
            }
        }
    }
    
    object SidebarRenderer : ColoredListCellRenderer<SidebarRenderer.CellItem>() {
        override fun customizeCellRenderer(
            list: JList<out CellItem?>,
            value: CellItem?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            if (value == null) return
            icon = value.icon
            append(value.label, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
        
        private fun readResolve(): Any = SidebarRenderer
        
        enum class CellItem(val label: String, val icon: Icon) {
            LOCAL("Local JAR", AllIcons.FileTypes.Archive),
            MODRINTH("Modrinth", AllIcons.Actions.Download),
            JENKINS("Jenkins", AllIcons.Actions.Download)
        }
    }
}