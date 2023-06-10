package com.jessebrault.gst.parser;

import com.jessebrault.gst.tokenizer.TokenProvider;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;
import com.jessebrault.gst.util.SimpleDiagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.jessebrault.gst.ast.TreeNodeType.*;

/**
 * May be extended.
 */
public class StandardGstParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(StandardGstParser.class);
    private static final Marker enter = MarkerFactory.getMarker("ENTER");
    private static final Marker exit = MarkerFactory.getMarker("EXIT");

    protected TokenProvider tokenProvider;
    protected ParserAccumulator acc;
    private List<TokenType> stashedTypes = new ArrayList<>();

    @Override
    public final void parse(TokenProvider tokenProvider, ParserAccumulator acc) {
        logger.trace(enter, "tokenProvider: {}, acc: {}", tokenProvider, acc);
        this.tokenProvider = tokenProvider;
        this.acc = acc;
        this.stashedTypes = new ArrayList<>();
        this.gString();
        logger.trace(exit, "");
    }

    private static String getDiagnosticMessage(String prefix, TokenType... expected) {
        final var b = new StringBuilder(prefix);
        if (expected.length == 0) {
            return b.append(".").toString();
        } else if (expected.length == 1) {
            return b.append("; expected ").append(expected[0]).append(".").toString();
        } else {
            b.append("; expected any of ");
            for (int i = 0; i < expected.length; i++) {
                b.append(expected[i]);
                if (i + 1 == expected.length) {
                    b.append(".");
                } else {
                    b.append(", ");
                }
            }
            return b.toString();
        }
    }
    protected static String getUnexpectedMessage(TokenType actual, TokenType... expected) {
        return getDiagnosticMessage("Unexpected token: " + actual, expected);
    }

    protected static String getNullTokenMessage(TokenType... expected) {
        return getDiagnosticMessage("Ran out of tokens", expected);
    }

    protected final void stash(TokenType type) {
        this.stashedTypes.add(type);
    }

    protected final boolean isStashed(TokenType... expectedTypes) {
        for (int i = 0; i < expectedTypes.length; i++) {
            final var expectedType = expectedTypes[i];
            if (i >= this.stashedTypes.size()) {
                return false;
            }
            final var stashedType = this.stashedTypes.get(i);
            if (expectedType != stashedType) {
                return false;
            }
        }
        return true;
    }

    protected final void clearStash() {
        this.stashedTypes.clear();
    }

    protected final Collection<Diagnostic> expectLeaf(boolean stash, TokenType... anyOf) {
        final Collection<Diagnostic> diagnostics = new ArrayList<>();
        final var currentType = this.tokenProvider.getCurrentType();
        if (currentType != null && Arrays.asList(anyOf).contains(currentType)) {
            if (stash) {
                this.stash(currentType);
            }
            this.acc.leaf(currentType, this.tokenProvider.getCurrentStart(), this.tokenProvider.getCurrentEnd());
        } else if (currentType != null) {
            final var diagnostic = new SimpleDiagnostic(
                    getUnexpectedMessage(currentType, anyOf)
            );
            this.acc.leaf(
                    currentType,
                    this.tokenProvider.getCurrentStart(),
                    this.tokenProvider.getCurrentEnd(),
                    List.of(diagnostic)
            );
        } else {
            diagnostics.add(new SimpleDiagnostic(
                    getNullTokenMessage(anyOf)
            ));
        }
        this.tokenProvider.advance();
        return diagnostics;
    }

    protected final Collection<Diagnostic> expectLeaf(TokenType... expectedTypes) {
        return this.expectLeaf(false, expectedTypes);
    }

    protected final Collection<Diagnostic> expectLeavesInOrder(TokenType... expectedTypes) {
        for (final var expectedType : expectedTypes) {
            final var diagnostics = this.expectLeaf(expectedType);
            if (!diagnostics.isEmpty()) {
                return diagnostics;
            }
        }
        return List.of();
    }

    protected void gString() {
        this.acc.start(G_STRING);
        final Collection<Diagnostic> gStringDiagnostics = new ArrayList<>();
        TokenType currentType;
        while ((currentType = this.tokenProvider.getCurrentType()) != null) {
            switch (currentType) {
                case TEXT -> this.text();
                case IMPORT_BLOCK_OPEN -> this.importBlock();
                case BLOCK_SCRIPTLET_OPEN -> this.blockScriptlet();
                case EXPRESSION_SCRIPTLET_OPEN -> this.expressionScriptlet();
                case DOLLAR_REFERENCE_DOLLAR -> this.dollarReference();
                case DOLLAR_SCRIPTLET_OPEN -> this.dollarScriptlet();
                default -> gStringDiagnostics.add(new SimpleDiagnostic(
                        getUnexpectedMessage(
                                currentType,
                                TokenType.TEXT,
                                TokenType.IMPORT_BLOCK_OPEN,
                                TokenType.BLOCK_SCRIPTLET_OPEN,
                                TokenType.EXPRESSION_SCRIPTLET_OPEN,
                                TokenType.DOLLAR_REFERENCE_DOLLAR,
                                TokenType.DOLLAR_SCRIPTLET_OPEN
                        )
                ));
            }
        }
        this.acc.done(gStringDiagnostics);
    }

    protected void text() {
        this.expectLeaf(TokenType.TEXT);
    }

    protected boolean isImportBlockPermitted() {
        return false;
    }

    protected void importBlock() {
        this.acc.start(IMPORT_BLOCK);
        final Collection<Diagnostic> diagnostics = new ArrayList<>();

        if (!this.isImportBlockPermitted()) {
            diagnostics.add(
                    new SimpleDiagnostic("ImportBlocks are not permitted by the StandardGStringParser.")
            );
        }

        // open
        final var openDiagnostics = this.expectLeaf(TokenType.IMPORT_BLOCK_OPEN);
        if (openDiagnostics.isEmpty()) {
            // body or close
            final var bodyOrCloseDiagnostics = this.expectLeaf(
                    true, TokenType.IMPORT_BLOCK_BODY, TokenType.IMPORT_BLOCK_CLOSE
            );
            if (bodyOrCloseDiagnostics.isEmpty()) {
                if (this.isStashed(TokenType.IMPORT_BLOCK_CLOSE)) {
                    // was closed
                    this.clearStash();
                    this.acc.done(diagnostics);
                } else {
                    // had body
                    this.clearStash();
                    diagnostics.addAll(this.expectLeaf(TokenType.IMPORT_BLOCK_CLOSE));
                    this.acc.done(diagnostics);
                }
            } else {
                this.clearStash();
                diagnostics.addAll(bodyOrCloseDiagnostics);
                this.acc.done(diagnostics);
            }
        } else {
            diagnostics.addAll(openDiagnostics);
            this.acc.done(diagnostics);
        }
    }

    protected void blockScriptlet() {
        this.acc.start(BLOCK_SCRIPTLET);
        // open
        final var openDiagnostics = this.expectLeaf(TokenType.BLOCK_SCRIPTLET_OPEN);
        if (openDiagnostics.isEmpty()) {
            this.scriptletBodyAndClose();
        } else {
            this.acc.done(openDiagnostics);
        }
    }

    protected void expressionScriptlet() {
        this.acc.start(EXPRESSION_SCRIPTLET);
        // open
        final var openDiagnostics = this.expectLeaf(TokenType.EXPRESSION_SCRIPTLET_OPEN);
        if (openDiagnostics.isEmpty()) {
            this.scriptletBodyAndClose();
        } else {
            this.acc.done(openDiagnostics);
        }
    }

    protected void scriptletBodyAndClose() {
        // body or close
        final var bodyOrCloseDiagnostics = this.expectLeaf(
                true, TokenType.SCRIPTLET_BODY, TokenType.SCRIPTLET_CLOSE
        );
        if (bodyOrCloseDiagnostics.isEmpty()) {
            if (this.isStashed(TokenType.SCRIPTLET_CLOSE)) {
                // was closed
                this.clearStash();
                this.acc.done();
            } else {
                // had body;
                this.clearStash();
                this.acc.done(this.expectLeaf(TokenType.SCRIPTLET_CLOSE));
            }
        } else {
            this.clearStash();
            this.acc.done(bodyOrCloseDiagnostics);
        }
    }

    protected void dollarReference() {
        this.acc.start(DOLLAR_REFERENCE);
        this.acc.done(this.expectLeavesInOrder(TokenType.DOLLAR_REFERENCE_DOLLAR, TokenType.DOLLAR_REFERENCE_BODY));
    }

    protected void dollarScriptlet() {
        this.acc.start(DOLLAR_SCRIPTLET);
        // open
        final var openDiagnostics = this.expectLeaf(TokenType.DOLLAR_SCRIPTLET_OPEN);
        if (openDiagnostics.isEmpty()) {
            // body or close
            final var bodyOrCloseDiagnostics = this.expectLeaf(
                    true, TokenType.DOLLAR_SCRIPTLET_BODY, TokenType.DOLLAR_SCRIPTLET_CLOSE
            );
            if (bodyOrCloseDiagnostics.isEmpty()) {
                if (this.isStashed(TokenType.DOLLAR_SCRIPTLET_CLOSE)) {
                    // was closed
                    this.clearStash();
                    this.acc.done();
                } else {
                    // had body
                    this.clearStash();
                    this.acc.done(this.expectLeaf(TokenType.DOLLAR_SCRIPTLET_CLOSE));
                }
            } else {
                this.clearStash();
                this.acc.done(bodyOrCloseDiagnostics);
            }
        } else {
            this.acc.done(openDiagnostics);
        }
    }

    @Override
    public String toString() {
        return "StandardGstParser()";
    }

}
