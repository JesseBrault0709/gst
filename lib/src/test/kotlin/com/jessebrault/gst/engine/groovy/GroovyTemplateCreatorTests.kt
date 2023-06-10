package com.jessebrault.gst.engine.groovy

import com.jessebrault.gst.engine.TemplateCreator
import com.jessebrault.gst.parser.Parser
import com.jessebrault.gst.parser.StandardGstParser
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals

import com.jessebrault.gst.parser.ExtendedGstParser
import com.jessebrault.gst.util.assertNoDiagnostics

class GroovyTemplateCreatorTests {

    @Suppress("unused")
    class Greeter(private val greeting: String = "Hello, World!") {
        fun greet() = this.greeting
    }

    private fun getCreator(
            parser: Parser = StandardGstParser(),
            urls: Collection<URL> = emptyList(),
            parentClassLoader: ClassLoader = this.javaClass.classLoader,
            printScript: Boolean = true
    ): TemplateCreator = GroovyTemplateCreator({ parser }, urls, parentClassLoader, printScript)

    private fun doStandardTest(input: String, expectedOutput: String) {
        val createResult = this.getCreator().create(input)
        assertNoDiagnostics(createResult)
        assertEquals(expectedOutput, createResult.get().make())
    }

    @Test
    fun helloWorld() = this.doStandardTest("Hello, World!", "Hello, World!")

    @Test
    fun dollarReference() {
        val createResult = this.getCreator().create("\$test")
        assertNoDiagnostics(createResult)
        assertEquals("Hello, World!", createResult.get().make(mapOf("test" to "Hello, World!")))
    }

    @Test
    fun importAlreadyOnClasspath() {
        val input =
"""
<%@ import com.jessebrault.gst.engine.groovy.GroovyTemplateCreatorTests.Greeter %>
<%= new Greeter().greet() %>
"""
        val result = this.getCreator(parser = ExtendedGstParser()).create(input)
        assertNoDiagnostics(result)
        val template = result.get()
        val output = template.make()
        assertEquals("Hello, World!", output.trim())
    }

    @Test
    fun importAvailableViaUrl() {
        val input = "<%@ import com.jessebrault.gst.tmp.Test %><%= new Test().greeting %>"
        val classDirUrl = writeClass(
                "Test",
                listOf("com", "jessebrault", "gst", "tmp"),
                "package com.jessebrault.gst.tmp\n\nclass Test { String greeting = 'Hello, World!' }"
        )
        val result = this.getCreator(parser = ExtendedGstParser(), urls = listOf(classDirUrl)).create(input)
        assertNoDiagnostics(result)
        val template = result.get()
        val output = template.make()
        assertEquals("Hello, World!", output)
    }

    @Test
    fun blockScriptletHasAccessToOut() = this.doStandardTest(
            "<% out << 'Hello, World!' %>", "Hello, World!"
    )

    @Test
    fun expressionScriptletOutputsValue() = this.doStandardTest(
            "<%= 'Hello, World!' %>", "Hello, World!"
    )

    @Test
    fun dollarScriptletOutputsValue() = this.doStandardTest(
            "\${ 'Hello, World!' }", "Hello, World!"
    )

}