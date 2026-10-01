package com.dfsek.terra.codetool.project

import com.dfsek.terra.codetool.TerrascriptFileType
import com.dfsek.terra.codetool.psi.TerrascriptFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumDataDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping

val TYPE_INDEX_NAME = ID.create<IndexedConfigType, Void>("com.dfsek.terra.codetool.project.type")

class FileTypeIndexExtension : ScalarIndexExtension<IndexedConfigType>() {
    override fun getName(): ID<IndexedConfigType, Void> = TYPE_INDEX_NAME
    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter {
        it.fileType == YAMLFileType.YML || it.fileType == TerrascriptFileType
    }
    override fun dependsOnFileContent(): Boolean = true
    
    override fun getIndexer(): DataIndexer<IndexedConfigType, Void, FileContent> = FileTypeIndexer
    
    override fun getKeyDescriptor(): KeyDescriptor<IndexedConfigType> = EnumDataDescriptor(IndexedConfigType::class.java)
    override fun getVersion(): Int = 0
}

private object FileTypeIndexer : DataIndexer<IndexedConfigType, Void, FileContent> {
    override fun map(content: FileContent): Map<IndexedConfigType, Void?> {
        when (val psiFile = content.psiFile) {
            is YAMLFile -> {
                val root = psiFile.documents.firstOrNull()?.topLevelValue as? YAMLMapping ?: return emptyMap()
                val typeString = root.getKeyValueByKey("type")?.valueText ?: return emptyMap()
                val type = IndexedConfigType.fromString(typeString) ?: return emptyMap()
                return mapOf(type to null)
            }
            
            is TerrascriptFile -> {
                return mapOf(IndexedConfigType.INTELLIJ_PROCEDURAL_STRUCTURE to null)
            }
        }
        return emptyMap()
        
    }
    
}
