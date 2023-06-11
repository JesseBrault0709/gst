package com.jessebrault.gst.groovy

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.net.URL
import java.nio.file.Files

private val logger: Logger = LoggerFactory.getLogger("GroovyTemplateCreatorTestsUtilKt")

/**
 * Returns the URL to the directory containing the written class.
 */
fun writeClass(className: String, packageComponents: List<String>, source: String): URL {
    val tmpDirPath = Files.createTempDirectory("groovyTemplateCreatorTestsUtil")
    val packageFile = File(tmpDirPath.toFile(), packageComponents.reduce { acc, component ->
        "$acc${ File.separator }$component"
    })
    if (packageFile.mkdirs()) {
        val classSourceFile = File(packageFile, "$className.groovy")
        FileWriter(classSourceFile).use {
            it.write(source)
        }
        logger.debug("class {} written to {}", className, classSourceFile)
        return tmpDirPath.toUri().toURL()
    } else {
        throw RuntimeException("Unable to create all directories for $packageFile")
    }
}