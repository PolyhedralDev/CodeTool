package com.dfsek.terra.codetool.template

import com.dfsek.terra.codetool.TerrascriptLanguage
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

class TerrascriptLiveTemplateContext : TemplateContextType("Terrascript") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return templateActionContext.file.language.isKindOf(TerrascriptLanguage)
    }
}