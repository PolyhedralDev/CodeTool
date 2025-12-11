package com.dfsek.terra.codetool.envman.task.snippet

import com.dfsek.terra.codetool.envman.service.state.TerraEnvironment

fun interface TaskSnippet {
    fun apply(environment: TerraEnvironment)
}