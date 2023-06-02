package com.jessebrault.gst.parser

import com.jessebrault.gst.ast.TreeNodeType.*
import com.jessebrault.gst.tokenizer.*
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals

abstract class AbstractParserTests(private val parser: Parser) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AbstractParserTests::class.java)
    }

    protected open fun getTokenProvider(input: CharSequence): TokenProvider = FsmBasedTokenizer().apply {
        start(input, 0, input.length, TokenizerState.TEXT)
    }

    protected open fun doGStringTest(input: CharSequence, tests: NodeTester.() -> Unit) {
        val acc = SimpleParserAccumulator()
        this.parser.parse(this.getTokenProvider(input), acc)
        assertGString(acc.result, tests)
    }

    protected abstract val isImportBlockLegal: Boolean

    @Test
    fun helloWorld() {
        this.doGStringTest("Hello, World!") {
            hasChild(TokenType.TEXT, 0, 13)
        }
    }

    @Test
    fun emptyImportBlock() {
        if (this.isImportBlockLegal) {
            this.doGStringTest("<%@%>") {
                hasNoDiagnostics()
                hasChild(IMPORT_BLOCK) {
                    hasNoDiagnostics()
                    hasChild(TokenType.IMPORT_BLOCK_OPEN, 0, 3)
                    hasChild(TokenType.IMPORT_BLOCK_CLOSE, 3, 5)
                }
            }
        }
    }

    @Test
    fun importBlockWithSpace() {
        if (this.isImportBlockLegal) {
            this.doGStringTest("<%@ %>") {
                hasNoDiagnostics()
                hasChild(IMPORT_BLOCK) {
                    hasNoDiagnostics()
                    hasChild(TokenType.IMPORT_BLOCK_OPEN, 0, 3)
                    hasChild(TokenType.IMPORT_BLOCK_BODY, 3, 4)
                    hasChild(TokenType.IMPORT_BLOCK_CLOSE, 4, 6)
                }
            }
        }
    }

    @Test
    fun emptyBlockScriptlet() {
        this.doGStringTest("<%%>") {
            hasChild(BLOCK_SCRIPTLET) {
                hasChild(TokenType.BLOCK_SCRIPTLET_OPEN, 0, 2)
                hasChild(TokenType.SCRIPTLET_CLOSE, 2, 4)
            }
        }
    }

    @Test
    fun blockScriptletWithSpace() = this.doGStringTest("<% %>") {
        hasChild(BLOCK_SCRIPTLET) {
            hasChild(TokenType.BLOCK_SCRIPTLET_OPEN, 0, 2)
            hasChild(TokenType.SCRIPTLET_BODY, 2, 3)
            hasChild(TokenType.SCRIPTLET_CLOSE, 3, 5)
        }
    }

    @Test
    fun emptyExpressionScriptlet() = this.doGStringTest("<%=%>") {
        hasChild(EXPRESSION_SCRIPTLET) {
            hasChild(TokenType.EXPRESSION_SCRIPTLET_OPEN, 0, 3)
            hasChild(TokenType.SCRIPTLET_CLOSE, 3, 5)
        }
    }

    @Test
    fun expressionScriptletWithSpace() = this.doGStringTest("<%= %>") {
        hasChild(EXPRESSION_SCRIPTLET) {
            hasChild(TokenType.EXPRESSION_SCRIPTLET_OPEN, 0, 3)
            hasChild(TokenType.SCRIPTLET_BODY, 3, 4)
            hasChild(TokenType.SCRIPTLET_CLOSE, 4, 6)
        }
    }

    @Test
    fun dollarReference() = this.doGStringTest("\$test") {
        hasChild(DOLLAR_REFERENCE) {
            hasChild(TokenType.DOLLAR_REFERENCE_DOLLAR, 0, 1)
            hasChild(TokenType.DOLLAR_REFERENCE_BODY, 1, 5)
        }
    }

    @Test
    fun emptyDollarScriptlet() = this.doGStringTest("\${}") {
        hasChild(DOLLAR_SCRIPTLET) {
            hasChild(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2)
            hasChild(TokenType.DOLLAR_SCRIPTLET_CLOSE, 2, 3)
        }
    }

    @Test
    fun dollarScriptletWithSpace() = this.doGStringTest("\${ }") {
        hasChild(DOLLAR_SCRIPTLET) {
            hasChild(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2)
            hasChild(TokenType.DOLLAR_SCRIPTLET_BODY, 2, 3)
            hasChild(TokenType.DOLLAR_SCRIPTLET_CLOSE, 3, 4)
        }
    }

    @Test
    fun unclosedDollarScriptlet() = this.doGStringTest("\${") {
        hasChild(DOLLAR_SCRIPTLET) {
            hasDiagnostics {
                logger.debug("diagnostics: {}", it)
                assertEquals(1, it.size)
            }
            hasChild(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2)
        }
    }

}