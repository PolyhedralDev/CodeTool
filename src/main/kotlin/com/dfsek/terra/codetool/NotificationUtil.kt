package com.dfsek.terra.codetool

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun showTerraBalloon(project: Project, title: String, content: String, type: NotificationType) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("Terra Environment Notifications")
        .createNotification(title, content, type)
        .notify(project)
}