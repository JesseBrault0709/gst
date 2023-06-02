package com.jessebrault.gst.tokenizer

import org.junit.jupiter.api.Test
import com.jessebrault.gst.tokenizer.TokenType.*
import kotlin.test.assertTrue

abstract class AbstractTokenizerTestsKt(private val tokenizer: Tokenizer) {

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun assertTokens(input: CharSequence, tests: TokenizerTester.() -> Unit) {
        val tester = TokenizerTester(this.tokenizer, input)
        tests(tester)
        assertTrue(tester.isDone())
    }

    @Test
    fun helloWorld() = assertTokens("Hello, World!") {
        token(TEXT, 0, 13)
    }

    @Test
    fun htmlLangEn() = assertTokens("<html lang=\"en\">") {
        token(TEXT, 0, 16)
    }

    @Test
    fun dollarReference() = assertTokens("\$a") {
        token(DOLLAR_REFERENCE_DOLLAR, 0, 1)
        token(DOLLAR_REFERENCE_BODY, 1, 2)
    }

    @Test
    fun complexDollarReference() = assertTokens("\$a.b.c") {
        token(DOLLAR_REFERENCE_DOLLAR, 0, 1)
        token(DOLLAR_REFERENCE_BODY, 1, 6)
    }

    @Test
    fun dollarReferenceFollowedByText() = assertTokens("\$greeting, world!") {
        token(DOLLAR_REFERENCE_DOLLAR, 0, 1)
        token(DOLLAR_REFERENCE_BODY, 1, 9)
        token(TEXT, 9, 17)
    }

    @Test
    fun dollarNotConfusedAsDollarReference() = assertTokens("$") {
        token(TEXT, 0, 1)
    }

    @Test
    fun empty() = assertTokens("") { }

    @Test
    fun emptyBlockScriptlet() = assertTokens("<%%>") {
        token(BLOCK_SCRIPTLET_OPEN, 0, 2)
        token(SCRIPTLET_CLOSE, 2, 4)
    }

    @Test
    fun emptyExpressionScriptlet() = assertTokens("<%=%>") {
        token(EXPRESSION_SCRIPTLET_OPEN, 0, 3)
        token(SCRIPTLET_CLOSE, 3, 5)
    }

    @Test
    fun unclosedBlockScriptlet() = assertTokens("<%") {
        token(BLOCK_SCRIPTLET_OPEN, 0, 2)
    }

    @Test
    fun unclosedExpressionScriptlet() = assertTokens("<%=") {
        token(EXPRESSION_SCRIPTLET_OPEN, 0, 3)
    }

    @Test
    fun unclosedBlockScriptletWithBody() = assertTokens("<% ") {
        token(BLOCK_SCRIPTLET_OPEN, 0, 2)
        token(SCRIPTLET_BODY, 2, 3)
    }

    @Test
    fun unclosedExpressionScriptletWithBody() = assertTokens("<%= ") {
        token(EXPRESSION_SCRIPTLET_OPEN, 0, 3)
        token(SCRIPTLET_BODY, 3, 4)
    }

    @Test
    fun simpleBlockScriptlet() = assertTokens("<% %>") {
        token(BLOCK_SCRIPTLET_OPEN, 0, 2)
        token(SCRIPTLET_BODY, 2, 3)
        token(SCRIPTLET_CLOSE, 3, 5)
    }

    @Test
    fun simpleExpressionScriptlet() = assertTokens("<%= %>") {
        token(EXPRESSION_SCRIPTLET_OPEN, 0, 3)
        token(SCRIPTLET_BODY, 3, 4)
        token(SCRIPTLET_CLOSE, 4, 6)
    }

    @Test
    fun textWithNewline() = assertTokens("\n") {
        token(TEXT, 0, 1)
    }

    @Test
    fun textWithMultipleNewlines() = assertTokens("\n\n") {
        token(TEXT, 0, 2)
    }

    @Test
    fun emptyImportBlock() = assertTokens("<%@%>") {
        token(IMPORT_BLOCK_OPEN, 0, 3)
        token(IMPORT_BLOCK_CLOSE, 3, 5)
    }

    @Test
    fun importBlockWithSpace() = assertTokens("<%@ %>") {
        token(IMPORT_BLOCK_OPEN, 0, 3)
        token(IMPORT_BLOCK_BODY, 3, 4)
        token(IMPORT_BLOCK_CLOSE, 4, 6)
    }

    @Test
    fun unclosedImportBlock() = assertTokens("<%@") {
        token(IMPORT_BLOCK_OPEN, 0, 3)
    }

    @Test
    fun unclosedImportBlockWithSpace() = assertTokens("<%@ ") {
        token(IMPORT_BLOCK_OPEN, 0, 3)
        token(IMPORT_BLOCK_BODY, 3, 4)
    }

    @Test
    fun emptyDollarScriptlet() = assertTokens("\${}") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
        token(DOLLAR_SCRIPTLET_CLOSE, 2, 3)
    }

    @Test
    fun dollarScriptletWithBody() = assertTokens("\${ }") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
        token(DOLLAR_SCRIPTLET_BODY, 2, 3)
        token(DOLLAR_SCRIPTLET_CLOSE, 3, 4)
    }

    @Test
    fun dollarScriptletWithNestedClosure() = assertTokens("\${ { } }") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
        token(DOLLAR_SCRIPTLET_BODY, 2, 7)
        token(DOLLAR_SCRIPTLET_CLOSE, 7, 8)
    }

    @Test
    fun dollarScriptletWithGString() = assertTokens("\${ \"\" }") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
        token(DOLLAR_SCRIPTLET_BODY, 2, 6)
        token(DOLLAR_SCRIPTLET_CLOSE, 6, 7)
    }

    @Test
    fun dollarScriptletWithGStringWithNestedClosure() = assertTokens("\${ \"\${ test() }\" }") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
        token(DOLLAR_SCRIPTLET_BODY, 2, 17)
        token(DOLLAR_SCRIPTLET_CLOSE, 17, 18)
    }

    @Test
    fun unclosedDollarScriptlet() = assertTokens("\${") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
    }

    @Test
    fun unclosedDollarScriptletWithBody() = assertTokens("\${ ") {
        token(DOLLAR_SCRIPTLET_OPEN, 0, 2)
        token(DOLLAR_SCRIPTLET_BODY, 2, 3)
    }

}