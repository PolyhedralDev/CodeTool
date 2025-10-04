package com.dfsek.terra.codetool.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.dfsek.terra.codetool.TerrascriptLanguage

class TerrascriptTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (!file.language.isKindOf(TerrascriptLanguage)) {
            return Result.CONTINUE
        }

        if (c == '"') {
            val document = editor.document
            val offset = editor.caretModel.offset

            if (offset < document.textLength && document.charsSequence[offset] == '"') {
                editor.caretModel.moveToOffset(offset + 1)
                return Result.STOP
            }

            document.insertString(offset, "\"")
            return Result.CONTINUE
        }
        
        if (c == '*') {
            val document = editor.document
            val offset = editor.caretModel.offset
            
            if (offset > 0 && document.charsSequence[offset - 2] == '/') {
                document.insertString(offset, "/")
                document.insertString(offset, "*")
                editor.caretModel.moveToOffset(offset)
                return Result.STOP
            }
        }
        return Result.CONTINUE
    }
}
