package com.dfsek.terra.codetool.envman.service.version

import com.dfsek.terra.codetool.showTerraBalloon
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.util.download.DownloadableFileService
import java.io.File

fun downloadTerraJar(project: Project, version: TerraVersion, targetDir: String): File? {
    val downloadService = DownloadableFileService.getInstance()
    val desc = downloadService.createFileDescription(version.downloadUrl, "terra.jar")
    val downloader = downloadService.createDownloader(listOf(desc), "Downloading Terra ${version.version}")
    val result = downloader.downloadFilesWithProgress(targetDir, project, null)
    if (result == null) {
        showTerraBalloon(project, "Error downloading Terra", "Please try again later", NotificationType.ERROR)
        return null
    }
    return File(targetDir, "terra.jar")
}