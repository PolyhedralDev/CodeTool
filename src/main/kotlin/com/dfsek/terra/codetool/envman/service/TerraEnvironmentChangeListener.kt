package com.dfsek.terra.codetool.envman.service

import com.intellij.util.messages.Topic

interface TerraEnvironmentChangeListener {
    companion object {
        val TOPIC = Topic.create("Terra Environment Changed", TerraEnvironmentChangeListener::class.java)
    }
    
    fun environmentChanged()
}