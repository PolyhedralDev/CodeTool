package com.dfsek.terra.codetool.envman.service

data class TerraEnvironment(
    var name: String,
    var path: String,
    var terraVersion: String
)

data class TerraEnvironmentState(
    var environments: MutableList<TerraEnvironment> = mutableListOf()
)
