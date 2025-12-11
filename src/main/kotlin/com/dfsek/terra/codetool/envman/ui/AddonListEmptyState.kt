package com.dfsek.terra.codetool.envman.ui

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.SingleComponentCenteringLayout
import com.intellij.util.ui.UIUtil
import javax.swing.JPanel
import javax.swing.SwingConstants

class AddonListEmptyState : JPanel(SingleComponentCenteringLayout()) {
    init {
        border = JBUI.Borders.emptyLeft(1)
        
        add(JBLabel("Select an environment to view addons").apply {
            foreground = UIUtil.getContextHelpForeground()
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
        })
    }
}