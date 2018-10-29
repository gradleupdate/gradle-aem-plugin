package com.cognifide.gradle.aem.instance.action

import com.cognifide.gradle.aem.api.AemExtension
import com.cognifide.gradle.aem.instance.*
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class AbstractAction(val project: Project) : InstanceAction {

    @Internal
    @Transient
    val aem = AemExtension.of(project)

    @Internal
    @Transient
    val logger = project.logger

    @Input
    var instances: List<Instance> = Instance.filter(project)

    @Internal
    var notify = true

    @get:Internal
    val handles = instances.toLocalHandles(project)

    fun notify(title: String, text: String, enabled: Boolean = this.notify) {
        if (enabled) {
            aem.notifier.default(title, text)
        } else {
            aem.notifier.log(title, text)
        }
    }

}