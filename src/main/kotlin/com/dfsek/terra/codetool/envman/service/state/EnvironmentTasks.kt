package com.dfsek.terra.codetool.envman.service.state

import com.dfsek.terra.codetool.envman.task.snippet.ExtractTaskSnippet
import com.dfsek.terra.codetool.envman.task.snippet.TaskSnippet

enum class EnvironmentTasks(
    val display: String,
    val snippet: TaskSnippet
) {
    EXTRACT("Extract", ExtractTaskSnippet)
}