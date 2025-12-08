package com.dfsek.terra.codetool.envman.service.version

interface TerraVersionExplorer {
    suspend fun getVersions(): List<TerraVersion>
}