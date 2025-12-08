package com.dfsek.terra.codetool.envman.service.state

data class TerraEnvironment(
    var id: String = "",
    var name: String = "",
    var path: String = "",
    var terraVersion: String = "",
    var state: EnvironmentState = EnvironmentState.UNINITIALIZED
)