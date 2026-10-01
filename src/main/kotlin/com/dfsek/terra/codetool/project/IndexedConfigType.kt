package com.dfsek.terra.codetool.project

enum class IndexedConfigType(val isPublic: Boolean = true) {
    BIOME,
    FEATURE,
    ORE,
    PALETTE,
    SCATTERED_ORE,
    INTELLIJ_PROCEDURAL_STRUCTURE(false);
    
    companion object {
        fun fromString(s: String, requirePublic: Boolean = true): IndexedConfigType? {
            val entry = entries.find { it.name.equals(s, ignoreCase = true) } ?: return null
            if (!entry.isPublic && requirePublic) return null
            return entry
        }
    }
    
}