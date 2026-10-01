package com.dfsek.terra.codetool.envman.ui.create

import com.dfsek.terra.codetool.envman.service.VersionDetectionService
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import javax.swing.JPanel

class LocalPanel(project: Project, val onChange: () -> Unit) : JPanel(BorderLayout(0, 20)) {
    private val versionDetector = VersionDetectionService.getInstance()
    
    private val pathField = TextFieldWithBrowseButton()
    private val metadataPanel = JPanel(GridBagLayout())
    
    private val versionLabel = JBLabel("Version: Select a JAR")
    private val statusLabel = JBLabel("Ready to index").apply {
        foreground = UIUtil.getInactiveTextColor()
    }
    
    private var isOk = false
    
    init {
        border = JBUI.Borders.empty(20)
        
        val descriptor = FileChooserDescriptor(true, false, true, true, false, false)
            .withFileFilter { it.extension == "jar" }
        
        pathField.addBrowseFolderListener(project, descriptor)
        
        pathField.textField.document.addDocumentListener(object : com.intellij.ui.DocumentAdapter() {
            override fun textChanged(e: javax.swing.event.DocumentEvent) {
                updateMetadataState(pathField.text)
                onChange()
            }
        })
        
        val header = JPanel(BorderLayout()).apply {
            val title = JBLabel("Local source").apply {
                font = font.deriveFont(Font.BOLD, 14f)
            }
            add(title, BorderLayout.NORTH)
            add(JBLabel("Point to a local terra-api or fat JAR file.").apply {
                foreground = UIUtil.getContextHelpForeground()
            }, BorderLayout.SOUTH)
        }
        
        setupMetadataPanel()
        
        val centerPanel = JPanel(BorderLayout(0, 15))
        centerPanel.add(pathField, BorderLayout.NORTH)
        centerPanel.add(metadataPanel, BorderLayout.CENTER)
        
        add(header, BorderLayout.NORTH)
        add(centerPanel, BorderLayout.CENTER)
    }
    
    private fun setupMetadataPanel() {
        metadataPanel.border = JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor.border()),
            JBUI.Borders.empty(15)
        )
        
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridx = 0
            gridy = 0
            insets = JBUI.insetsBottom(5)
        }
        
        metadataPanel.add(versionLabel.apply { font = font.deriveFont(Font.BOLD) }, gbc)
        gbc.gridy++
        metadataPanel.add(statusLabel, gbc)
    }
    
    private fun updateMetadataState(path: String) {
        if (path.isEmpty()) {
            versionLabel.text = "Version: Select a JAR"
            statusLabel.text = "Waiting for source..."
            statusLabel.foreground = UIUtil.getInactiveTextColor()
            isOk = false
            return
        }
        
        val version = versionDetector.detectVersion(File(path))
        if (version == null) {
            versionLabel.text = "Error: Invalid JAR"
            statusLabel.text = "Please insert a valid Terra Fabric jar"
            statusLabel.foreground = JBColor.RED
            isOk = false
            return
        }
        
        versionLabel.text = "Detected: Terra $version"
        statusLabel.text = "✓ Valid engine JAR identified"
        statusLabel.foreground = JBColor.GREEN
        isOk = true
    }
    
    fun getSelectedPath(): String? = if (isOk) pathField.text.trim() else null
}