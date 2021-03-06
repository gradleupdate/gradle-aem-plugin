package com.cognifide.gradle.aem.instance.action

import com.cognifide.gradle.aem.common.AemExtension
import com.cognifide.gradle.aem.instance.Instance
import com.cognifide.gradle.aem.instance.InstanceAction
import com.cognifide.gradle.aem.instance.LocalInstance
import org.gradle.api.tasks.Internal

abstract class AbstractAction(
    @Internal
    @Transient
    val aem: AemExtension
) : InstanceAction {

    var enabled = true

    var instances: List<Instance> = aem.instances

    val localInstances: List<LocalInstance>
        get() = instances.filterIsInstance(LocalInstance::class.java)

    var notify = true

    fun notify(title: String, text: String, enabled: Boolean = this.notify) {
        if (enabled) {
            aem.notifier.notify(title, text)
        } else {
            aem.notifier.log(title, text)
        }
    }
}