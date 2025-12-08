package com.dfsek.terra.codetool.envman.service.download

interface TerraVersionExplorer {
    suspend fun getVersions(): List<TerraVersion>
}