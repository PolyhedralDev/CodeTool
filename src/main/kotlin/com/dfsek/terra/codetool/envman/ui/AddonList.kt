package com.dfsek.terra.codetool.envman.ui

import com.dfsek.terra.codetool.envman.service.state.TerraEnvironment
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class AddonList(env: TerraEnvironment) : JPanel(BorderLayout()) {
    private val terraVersionHeader = JBLabel("${env.name} @${env.terraVersion}").apply {
        font = JBUI.Fonts.label().asBold()
        border = JBUI.Borders.empty(10, 5)
    }
    
    private val addonTable = JBTable(DefaultTableModel(arrayOf("Addon", "Version"), 0)).apply {
        border = JBUI.Borders.empty()
    }
    
    init {
        border = JBUI.Borders.emptyLeft(1)
        
        val headerContainer = JPanel(BorderLayout())
        headerContainer.add(terraVersionHeader, BorderLayout.WEST)
        
        headerContainer.border = JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0)
        
        add(headerContainer, BorderLayout.NORTH)
        add(JBScrollPane(addonTable).apply {
            border = JBUI.Borders.empty()
        }, BorderLayout.CENTER)
    }
}