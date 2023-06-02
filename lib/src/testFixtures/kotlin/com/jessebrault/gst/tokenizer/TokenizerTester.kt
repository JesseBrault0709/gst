package com.jessebrault.gst.tokenizer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals

class TokenizerTester(
        private val tokenizer: Tokenizer,
        input: CharSequence,
        start: Int = 0,
        end: Int = input.length,
        initialState: TokenizerState = TokenizerState.TEXT
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TokenizerTester::class.java)
    }

    private var index = 0

    init {
        this.tokenizer.start(input, start, end, initialState)
    }

    fun token(tokenType: TokenType, start: Int, end: Int) {
        logger.info(
                "index: {}, expectedType: {}, expectedStart: {}, expectedEnd: {}",
                this.index, tokenType, start, end
        )
        assertEquals(tokenType, this.tokenizer.currentType)
        assertEquals(start, this.tokenizer.currentStart)
        assertEquals(end, this.tokenizer.currentEnd)
        this.tokenizer.advance()
        index++
    }

    fun isDone() = this.tokenizer.currentType == null

}