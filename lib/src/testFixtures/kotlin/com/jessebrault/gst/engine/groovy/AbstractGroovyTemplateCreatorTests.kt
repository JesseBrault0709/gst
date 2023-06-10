package com.jessebrault.gst.engine.groovy

import com.jessebrault.gst.engine.TemplateCreator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

abstract class AbstractGroovyTemplateCreatorTests(
        private val creator: TemplateCreator
) {

    @Test
    fun helloWorld() {
        val input = "Hello, World!"
        val result = this.creator.create(input)
        assertFalse(result.hasDiagnostics(), "Diagnostics: ${ result.diagnostics }")
        val template = result.get()
        val output = template.make()
        assertEquals(input, output)
    }

    @Test
    fun dollarReference() {
        val input = "\$test"
        val result = this.creator.create(input)
        assertFalse(result.hasDiagnostics(), "Diagnostics: ${ result.diagnostics }")
        val template = result.get()
        val output = template.make(mapOf("test" to "Hello, World!"))
        assertEquals("Hello, World!", output)
    }

}