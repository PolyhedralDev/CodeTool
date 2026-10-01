package com.dfsek.terra.codetool.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

object ProjectRootUtility {
    fun getNearestRoot(file: VirtualFile, project: Project): ProjectRoot? {
        val roots = FileBasedIndex.getInstance().getContainingFiles(ROOT_INDEX_NAME, "base", GlobalSearchScope.projectScope(project))
        val nearest = roots.minByOrNull { root ->
            val commonPath = file.path.commonPrefixWith(root.path)
            root.path.length - commonPath.length
        }
        nearest ?: return null
        val addons = FileBasedIndex.getInstance().getFileData(ROOT_INDEX_NAME, nearest, project).keys
        val name = FileBasedIndex.getInstance().getFileData(NAME_INDEX_NAME, nearest, project).keys.firstOrNull() ?: return null
        return ProjectRoot(nearest, name, addons.toList())
    }
    
    data class ProjectRoot(val root: VirtualFile, val id: String, val addons: List<String>)
}