package com.cognifide.gradle.aem.common.file

import com.cognifide.gradle.aem.common.AemPlugin
import com.cognifide.gradle.aem.common.Patterns
import com.cognifide.gradle.aem.tooling.tail.TailException
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.gradle.api.Project
import org.gradle.util.GFileUtils
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.zeroturnaround.zip.ZipUtil

object FileOperations {

    fun <T> readResourceFromPathOrClasspath(resource: String, parser: (BufferedReader) -> T): T =
            BufferedReader(InputStreamReader(inputStreamFromPathOrClasspath(resource))).use(parser)

    fun <T> optionalReadResourceFromPathOrClasspath(resource: String, parser: (BufferedReader) -> T): T? =
            try {
                readResourceFromPathOrClasspath(resource, parser)
            } catch (e: TailException) {
                null
            }

    private fun inputStreamFromPathOrClasspath(resourcePath: String): InputStream {
        val resourceFile = File(resourcePath)
        if (resourceFile.exists()) {
            return GFileUtils.openInputStream(resourceFile)
        }
        val resourceStream: InputStream? = this::class.java.getResourceAsStream(resourcePath)
        if (resourceStream != null) {
            return resourceStream
        }
        val resource: URL? = this::class.java.classLoader.getResource(resourcePath)
        if (resource != null) {
            return GFileUtils.openInputStream(File(resource.file))
        }
        throw TailException("Cannot load blacklist from file: $resourcePath")
    }

    fun readResourceFromAemPkg(path: String): InputStream? {
        return javaClass.getResourceAsStream("/${AemPlugin.PKG.replace(".", "/")}/$path")
    }

    private fun getResourcesFromAemPkg(path: String): List<String> {
        return Reflections("${AemPlugin.PKG}.$path".replace("/", "."), ResourcesScanner()).getResources { true; }.toList()
    }

    private fun eachResourceFromAemPkg(resourceRoot: String, targetDir: File, callback: (String, File) -> Unit) {
        for (resourcePath in getResourcesFromAemPkg(resourceRoot)) {
            val outputFile = File(targetDir, resourcePath.substringAfterLast("$resourceRoot/"))

            callback(resourcePath, outputFile)
        }
    }

    fun copyResourcesFromAemPkg(resourceRoot: String, targetDir: File, skipExisting: Boolean = false) {
        eachResourceFromAemPkg(resourceRoot, targetDir) { resourcePath, outputFile ->
            if (!skipExisting || !outputFile.exists()) {
                copyResource(resourcePath, outputFile)
            }
        }
    }

    private fun copyResource(resourcePath: String, outputFile: File) {
        GFileUtils.mkdirs(outputFile.parentFile)

        javaClass.getResourceAsStream("/$resourcePath").use { input ->
            FileOutputStream(outputFile).use { output ->
                IOUtils.copy(input, output)
            }
        }
    }

    fun amendFiles(dir: File, wildcardFilters: List<String>, amender: (File, String) -> String) {
        val files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        files?.filter { Patterns.wildcard(it, wildcardFilters) }?.forEach { file ->
            val source = amender(file, file.inputStream().bufferedReader().use { it.readText() })
            file.printWriter().use { it.print(source) }
        }
    }

    fun amendFile(file: File, amender: (String) -> String) {
        val source = amender(file.inputStream().bufferedReader().use { it.readText() })
        file.printWriter().use { it.print(source) }
    }

    fun find(project: Project, dirIfFileName: String, pathOrFileNames: List<String>): File? {
        for (pathOrFileName in pathOrFileNames) {
            val file = find(project, dirIfFileName, pathOrFileName)
            if (file != null) {
                return file
            }
        }

        return null
    }

    fun find(project: Project, dirIfFileName: String, pathOrFileName: String): File? {
        if (pathOrFileName.isBlank()) {
            return null
        }

        return mutableListOf<(String) -> File>(
                { project.file(pathOrFileName) },
                { File(File(dirIfFileName), pathOrFileName) },
                { File(pathOrFileName) }
        ).map { it(pathOrFileName) }.firstOrNull { it.exists() }
    }

    fun find(dir: File, pattern: String): File? {
        return find(dir, listOf(pattern))
    }

    fun find(dir: File, patterns: List<String>): File? {
        var result: File? = null
        val files = dir.listFiles { _, name -> Patterns.wildcard(name, patterns) }
        if (files != null) {
            result = files.firstOrNull()
        }
        return result
    }

    fun isDirEmpty(dir: File): Boolean {
        return dir.exists() && isDirEmpty(Paths.get(dir.absolutePath))
    }

    fun isDirEmpty(dir: Path): Boolean {
        Files.newDirectoryStream(dir).use { dirStream -> return !dirStream.iterator().hasNext() }
    }

    fun zipCount(zip: File) = zipCount(zip, "")

    fun zipCount(zip: File, path: String): Long {
        var total = 0L
        ZipUtil.iterate(zip) { entry ->
            if (entry.name.startsWith(path)) {
                total++
            }
        }
        return total
    }

    fun zipUnpack(zip: File, targetDir: File, eachFileName: (String) -> Unit = {}) = zipUnpack(zip, targetDir, "", eachFileName)

    fun zipUnpack(zip: File, targetDir: File, path: String, eachFileName: (String) -> Unit = {}) {
        ZipUtil.unpack(zip, targetDir) { name ->
            if (name.startsWith(path)) {
                val fileName = name.substringAfterLast("/")
                eachFileName(fileName)
                name.substring(path.length)
            } else {
                name
            }
        }
    }
}