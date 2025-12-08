package com.dfsek.terra.codetool.envman.ui.create

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.SingleComponentCenteringLayout
import com.intellij.util.ui.UIUtil
import javax.swing.JPanel
import javax.swing.SwingConstants

class RemotePanelEmptyState : JPanel(SingleComponentCenteringLayout()) {
    init {
        border = JBUI.Borders.emptyLeft(1)
        
        add(JBLabel("No versions found").apply {
            foreground = UIUtil.getContextHelpForeground()
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
        })
    }
}