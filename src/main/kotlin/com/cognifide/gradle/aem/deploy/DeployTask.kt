package com.cognifide.gradle.aem.deploy

import org.gradle.api.tasks.TaskAction

open class DeployTask : AbstractTask() {

    companion object {
        val NAME = "aemDeploy"
    }

    @TaskAction
    fun deploy() {
        logger.info("Package deploy completed.")
        filterInstances().onEach { logger.info("Instance: $it") }
    }

}