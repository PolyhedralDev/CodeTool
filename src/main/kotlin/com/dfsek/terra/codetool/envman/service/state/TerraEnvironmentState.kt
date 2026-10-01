package com.dfsek.terra.codetool.envman.service.state

data class TerraEnvironmentState(
    var environments: MutableList<TerraEnvironment> = mutableListOf()
)
