package com.dfsek.terra.codetool.project

import com.dfsek.terra.codetool.TerrascriptFileType
import com.dfsek.terra.codetool.psi.TerrascriptFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping

val NAME_INDEX_NAME = ID.create<String, Void>("com.dfsek.terra.codetool.project.name")

class NameIndexExtension : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> = NAME_INDEX_NAME

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter {
        it.fileType == YAMLFileType.YML || it.fileType == TerrascriptFileType
    }

    override fun dependsOnFileContent(): Boolean = true

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = NameIndexer

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1
}

private object NameIndexer : DataIndexer<String, Void, FileContent> {
    override fun map(content: FileContent): Map<String, Void?> {
        when (val psiFile = content.psiFile) {
            is YAMLFile -> {
                val root = psiFile.documents.firstOrNull()?.topLevelValue as? YAMLMapping ?: return emptyMap()
                val name = root.getKeyValueByKey("id")?.valueText ?: return emptyMap()
                return mapOf(name to null)
            }
            is TerrascriptFile -> {
                return mapOf(content.file.nameWithoutExtension to null)
            }
        }
        return emptyMap()
    }
}