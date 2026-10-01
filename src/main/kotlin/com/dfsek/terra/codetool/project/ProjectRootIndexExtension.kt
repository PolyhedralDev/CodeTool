package com.dfsek.terra.codetool.project

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping

val ROOT_INDEX_NAME = ID.create<String, Void>("com.dfsek.terra.codetool.project.root")

class ProjectRootIndexExtension : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> = ROOT_INDEX_NAME
    override fun getInputFilter(): FileBasedIndex.InputFilter = DefaultFileTypeSpecificInputFilter(YAMLFileType.YML)
    override fun dependsOnFileContent(): Boolean = true
    
    override fun getIndexer(): DataIndexer<String, Void, FileContent> = RootIndexer
    
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getVersion(): Int = 0
}

private object RootIndexer : DataIndexer<String, Void, FileContent> {
    override fun map(content: FileContent): Map<String, Void?> {
        if (!content.fileName.endsWith("pack.yml") && !content.fileName.endsWith("pack.yaml")) return emptyMap()
        val psiFile = content.psiFile as? YAMLFile ?: return emptyMap()
        val root = psiFile.documents.firstOrNull()?.topLevelValue as? YAMLMapping? ?: return emptyMap()
        
        // A project root has an ìd, a version, an author
        if (root.getKeyValueByKey("id") == null || root.getKeyValueByKey("version") == null || root.getKeyValueByKey("author") == null)
            return emptyMap()
        
        // get addons. "base" is loaded by default
        val addons = (root.getKeyValueByKey("addons")?.value as? YAMLMapping)?.keyValues?.map { it.keyText } ?: return mapOf("base" to null)
        return addons.associateWith { null } + mapOf("base" to null)
    }
    
}