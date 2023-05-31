package com.jessebrault.gst.tokenizer;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTokenizerTests {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTokenizerTests.class);

    protected static final class AssertTokensBuilder {

        private final List<Token> tokens = new ArrayList<>();

        public void token(TokenType type, int startIndex, int endIndex) {
            this.tokens.add(new SimpleToken(type, startIndex, endIndex));
        }

        public List<Token> getTokens() {
            return this.tokens;
        }

    }

    protected final Tokenizer tokenizer;

    public AbstractTokenizerTests(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    protected void assertTokens(CharSequence input, Consumer<AssertTokensBuilder> testBuilder) {
        final var b = new AssertTokensBuilder();
        testBuilder.accept(b);
        final var expectedTokens = b.getTokens();
        final var actual = this.tokenizer.tokenize(input);
        logger.debug("expectedTokens: {}", expectedTokens);
        logger.debug("actual: {}", actual);
        assertIterableEquals(expectedTokens, actual);
    }

    @Test
    public void helloWorld() {
        assertTokens("Hello, World!", tb -> {
            tb.token(TokenType.TEXT, 0, 13);
        });
    }

    @Test
    public void htmlLangEn() {
        assertTokens("<html lang=\"en\">", tb -> {
            tb.token(TokenType.TEXT, 0, 16);
        });
    }

    @Test
    public void dollarReference() {
        assertTokens("$a", tb -> {
            tb.token(TokenType.DOLLAR_REFERENCE_DOLLAR, 0, 1);
            tb.token(TokenType.DOLLAR_REFERENCE_BODY, 1, 2);
        });
    }

    @Test
    public void complexDollarReference() {
        assertTokens("$a.b.c", tb -> {
            tb.token(TokenType.DOLLAR_REFERENCE_DOLLAR, 0, 1);
            tb.token(TokenType.DOLLAR_REFERENCE_BODY, 1, 6);
        });
    }

    @Test
    public void dollarReferenceFollowedByText() {
        assertTokens("$greeting, world!", tb -> {
            tb.token(TokenType.DOLLAR_REFERENCE_DOLLAR, 0, 1);
            tb.token(TokenType.DOLLAR_REFERENCE_BODY, 1, 9);
            tb.token(TokenType.TEXT, 9, 17);
        });
    }

    @Test
    public void dollarNotConfusedAsDollarReferenceDollar() {
        assertTokens("$", tb -> {
            tb.token(TokenType.TEXT, 0, 1);
        });
    }

    @Test
    public void empty() {
        assertTokens("", tb -> {});
    }

    @Test
    public void emptyBlockScriptlet() {
        assertTokens("<%%>", tb -> {
            tb.token(TokenType.BLOCK_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.SCRIPTLET_CLOSE, 2, 4);
        });
    }

    @Test
    public void emptyExpressionScriptlet() {
        assertTokens("<%=%>", tb -> {
            tb.token(TokenType.EXPRESSION_SCRIPTLET_OPEN, 0, 3);
            tb.token(TokenType.SCRIPTLET_CLOSE, 3, 5);
        });
    }

    @Test
    public void unclosedBlockScriptlet() {
        assertTokens("<%", tb -> {
            tb.token(TokenType.BLOCK_SCRIPTLET_OPEN, 0, 2);
        });
    }

    @Test
    public void unclosedExpressionBlockScriptlet() {
        assertTokens("<%=", tb -> {
            tb.token(TokenType.EXPRESSION_SCRIPTLET_OPEN, 0, 3);
        });
    }

    @Test
    public void unclosedBlockScriptletWithSpace() {
        assertTokens("<% ", tb -> {
            tb.token(TokenType.BLOCK_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.SCRIPTLET_BODY, 2, 3);
        });
    }

    @Test
    public void simpleBlockScriptlet() {
        assertTokens("<% out << 'Hello, World!' %>", tb -> {
            tb.token(TokenType.BLOCK_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.SCRIPTLET_BODY, 2, 26);
            tb.token(TokenType.SCRIPTLET_CLOSE, 26, 28);
        });
    }

    @Test
    public void simpleExpressionScriptlet() {
        assertTokens("<%= 'Hello, World!' %>", tb -> {
            tb.token(TokenType.EXPRESSION_SCRIPTLET_OPEN, 0, 3);
            tb.token(TokenType.SCRIPTLET_BODY, 3, 20);
            tb.token(TokenType.SCRIPTLET_CLOSE, 20, 22);
        });
    }

    @Test
    public void textWithNewline() {
        assertTokens("\n", tb -> {
            tb.token(TokenType.TEXT, 0, 1);
        });
    }

    @Test
    public void textWithMultipleNewlines() {
        assertTokens("\n\n", tb -> {
            tb.token(TokenType.TEXT, 0, 2);
        });
    }

    @Test
    public void emptyImportBlock() {
        assertTokens("<%@%>", tb -> {
            tb.token(TokenType.IMPORT_BLOCK_OPEN, 0, 3);
            tb.token(TokenType.IMPORT_BLOCK_CLOSE, 3, 5);
        });
    }

    @Test
    public void importBlockWithSpace() {
        assertTokens("<%@ %>", tb -> {
            tb.token(TokenType.IMPORT_BLOCK_OPEN, 0, 3);
            tb.token(TokenType.IMPORT_BLOCK_BODY, 3, 4);
            tb.token(TokenType.IMPORT_BLOCK_CLOSE, 4, 6);
        });
    }

    @Test
    public void unclosedImportBlock() {
        assertTokens("<%@", tb -> {
            tb.token(TokenType.IMPORT_BLOCK_OPEN, 0, 3);
        });
    }

    @Test
    public void emptyDollarScriptlet() {
        assertTokens("${}", tb -> {
            tb.token(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.DOLLAR_SCRIPTLET_CLOSE, 2, 3);
        });
    }

    @Test
    public void dollarScriptletWithSpace() {
        assertTokens("${ }", tb -> {
            tb.token(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.DOLLAR_SCRIPTLET_BODY, 2, 3);
            tb.token(TokenType.DOLLAR_SCRIPTLET_CLOSE, 3, 4);
        });
    }

    @Test
    public void dollarScriptletWithNestedClosure() {
        assertTokens("${ { } }", tb -> {
            tb.token(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.DOLLAR_SCRIPTLET_BODY, 2, 7);
            tb.token(TokenType.DOLLAR_SCRIPTLET_CLOSE, 7, 8);
        });
    }

    @Test
    public void dollarScriptletWithGString() {
        assertTokens("${ \"\" }", tb -> {
            tb.token(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.DOLLAR_SCRIPTLET_BODY, 2, 6);
            tb.token(TokenType.DOLLAR_SCRIPTLET_CLOSE, 6, 7);
        });
    }

    @Test
    public void dollarScriptletWithGStringWithNestedClosure() {
        assertTokens("${ \"${ test() }\" }", tb -> {
            tb.token(TokenType.DOLLAR_SCRIPTLET_OPEN, 0, 2);
            tb.token(TokenType.DOLLAR_SCRIPTLET_BODY, 2, 17);
            tb.token(TokenType.DOLLAR_SCRIPTLET_CLOSE, 17, 18);
        });
    }

}
