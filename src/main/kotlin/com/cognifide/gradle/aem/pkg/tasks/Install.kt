package com.cognifide.gradle.aem.pkg.tasks

import com.cognifide.gradle.aem.common.fileNames
import com.cognifide.gradle.aem.instance.names
import org.gradle.api.tasks.TaskAction

open class Install : Sync() {

    init {
        description = "Installs AEM package on instance(s)."
    }

    @TaskAction
    fun install() {
        aem.progress({
            header = "Installing package(s) on instance(s)"
            total = instances.size.toLong() * packages.size.toLong()
        }, {
            aem.syncPackages(instances, packages) { pkg ->
                increment("${pkg.name} -> ${instance.name}") {
                    installPackage(determineRemotePackagePath(pkg))
                }
            }
        })

        aem.notifier.notify("Package installed", "${packages.fileNames} from ${instances.names}")
    }

    companion object {
        const val NAME = "aemInstall"
    }
}