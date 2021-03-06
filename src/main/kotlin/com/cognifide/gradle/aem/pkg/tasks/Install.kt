package com.cognifide.gradle.aem.pkg.tasks

import com.cognifide.gradle.aem.common.fileNames
import com.cognifide.gradle.aem.common.tasks.PackageTask
import com.cognifide.gradle.aem.instance.names
import org.gradle.api.tasks.TaskAction

open class Install : PackageTask() {

    init {
        description = "Installs AEM package on instance(s)."
    }

    @TaskAction
    fun install() {
        aem.progress(instances.size * packages.size) {
            aem.syncPackages(instances, packages) { file ->
                increment("${file.name} -> ${instance.name}") {
                    val pkg = getPackage(file)
                    installPackage(pkg.path)
                }
            }
        }

        aem.notifier.notify("Package installed", "${packages.fileNames} from ${instances.names}")
    }

    companion object {
        const val NAME = "aemInstall"
    }
}
