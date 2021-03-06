package com.cognifide.gradle.aem.test

import com.cognifide.gradle.aem.test.AemAssert.assertJsonCustomized
import com.cognifide.gradle.aem.test.json.AnyValueMatcher
import com.cognifide.gradle.aem.test.json.PathValueMatcher
import com.cognifide.gradle.aem.test.json.ValueMatcher
import org.junit.jupiter.api.Test

class DebugTaskTest : AemTest() {

    @Test
    fun shouldGenerateValidJsonFileForMinimal() {
        buildDebugJsonFile("minimal")
    }

    @Test
    fun shouldGenerateValidJsonFileForAdditional() {
        buildDebugJsonFile("additional")
    }

    private fun buildDebugJsonFile(buildName: String) {
        build("debug/$buildName", {
            withArguments(":aemDebug", "-S", "-i", "-Paem.debug.packageDeployed=false")
        }, {
            assertJsonCustomized(
                    readFile("debug/$buildName/debug.json"),
                    readFile(file("build/aem/aemDebug/debug.json")),
                    JSON_CUSTOMIZATIONS
            )
        })
    }

    companion object {
        val JSON_CUSTOMIZATIONS = ValueMatcher.customizationsOf(mapOf(
                "buildInfo" to AnyValueMatcher(),
                "projectInfo.dir" to PathValueMatcher(),
                "baseConfig.localInstanceOptions.root" to PathValueMatcher(),
                "baseConfig.localInstanceOptions.overridesPath" to PathValueMatcher(),
                "baseConfig.packageRoot" to PathValueMatcher(),
                "baseConfig.packageMetaCommonRoot" to PathValueMatcher(),
                "baseConfig.groovyScriptRoot" to PathValueMatcher(),
                "bundleConfig[*].bndPath" to PathValueMatcher()
        ))
    }

}