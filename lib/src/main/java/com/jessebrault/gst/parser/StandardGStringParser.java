package com.jessebrault.gst.parser;

import com.jessebrault.gst.tokenizer.Token;
import com.jessebrault.gst.tokenizer.TokenProvider;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;
import com.jessebrault.gst.util.SimpleDiagnostic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.jessebrault.gst.ast.TreeNodeType.G_STRING;
import static com.jessebrault.gst.ast.TreeNodeType.IMPORT_BLOCK;

public class StandardGStringParser implements Parser {

    protected TokenProvider tokens;
    protected Accumulator acc;

    @Override
    public final void parse(TokenProvider tokenProvider, Accumulator acc) {
        this.tokens = tokenProvider;
        this.acc = acc;
        this.gString();
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

    protected void gString() {
        this.acc.start(G_STRING);
        final Collection<Diagnostic> gStringDiagnostics = new ArrayList<>();
        Token current;
        while ((current = this.tokens.getCurrent()) != null) {
            switch (current.getType()) {
                case TEXT -> this.text();
                case IMPORT_BLOCK_OPEN -> this.importBlock();
                case BLOCK_SCRIPTLET_OPEN -> this.blockScriptlet();
                case EXPRESSION_SCRIPTLET_OPEN -> this.expressionScriptlet();
                case DOLLAR_REFERENCE_DOLLAR -> this.dollarReference();
                case DOLLAR_SCRIPTLET_OPEN -> this.dollarScriptlet();
                default -> gStringDiagnostics.add(new SimpleDiagnostic(
                        getUnexpectedMessage(
                                current.getType(),
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
        final var t = this.tokens.getCurrent();
        if (t != null && t.getType() == TokenType.TEXT) {
            this.acc.leaf(TokenType.TEXT, t.getStartIndex(), t.getEndIndex());
        } else if (t != null) {
            final var diagnostic = new SimpleDiagnostic(getUnexpectedMessage(t.getType(), TokenType.TEXT));
            this.acc.leaf(t.getType(), t.getStartIndex(), t.getEndIndex(), List.of(diagnostic));
            this.tokens.advance();
        } else {
            final var diagnostic = new SimpleDiagnostic(getNullTokenMessage(TokenType.TEXT));
            this.acc.done(List.of(diagnostic));
        }
    }

    protected boolean isImportBlockPermitted() {
        return false;
    }

    protected void importBlock() {
        if (this.isImportBlockPermitted()) {
            this.acc.start(IMPORT_BLOCK);
        } else {
            final var diagnostic = new SimpleDiagnostic("ImportBlocks are not permitted by the StandardGStringParser.");
            this.acc.start(IMPORT_BLOCK, List.of(diagnostic));
        }

        final Collection<Diagnostic> importBlockDiagnostics = new ArrayList<>();

        final var t0 = this.tokens.getCurrent();
        if (t0 != null && t0.getType() == TokenType.IMPORT_BLOCK_OPEN) {
            this.acc.leaf(t0.getType(), t0.getStartIndex(), t0.getEndIndex());
            this.tokens.advance();
            final var t1 = this.tokens.getCurrent();
            if (t1 != null) {
                if (t1.getType() == TokenType.IMPORT_BLOCK_OPEN) {
                    this.acc.leaf(t1.getType(), t1.getStartIndex(), t1.getEndIndex());
                    this.tokens.advance();
                    final var t2 = this.tokens.getCurrent();
                    if (t2 != null && t2.getType() == TokenType.IMPORT_BLOCK_CLOSE) {
                        this.acc.leaf(t2.getType(), t2.getStartIndex(), t2.getEndIndex());
                    } else if (t2 != null) {
                        final var diagnostic = new SimpleDiagnostic(
                                getUnexpectedMessage(t2.getType(), TokenType.IMPORT_BLOCK_CLOSE)
                        );
                        this.acc.leaf(t2.getType(), t2.getStartIndex(), t2.getEndIndex(), List.of(diagnostic));
                    } else {
                        importBlockDiagnostics.add(new SimpleDiagnostic(
                                getNullTokenMessage(TokenType.IMPORT_BLOCK_CLOSE)
                        ));
                    }
                } else if (t1.getType() == TokenType.IMPORT_BLOCK_CLOSE) {
                    this.acc.leaf(t1.getType(), t1.getStartIndex(), t1.getEndIndex());
                } else {
                    final var diagnostic = new SimpleDiagnostic(
                            getUnexpectedMessage(t1.getType(), TokenType.IMPORT_BLOCK_BODY, TokenType.IMPORT_BLOCK_CLOSE)
                    );
                    this.acc.leaf(t1.getType(), t1.getStartIndex(), t1.getEndIndex(), List.of(diagnostic));
                }
            } else {
                importBlockDiagnostics.add(new SimpleDiagnostic(
                        getNullTokenMessage(TokenType.IMPORT_BLOCK_BODY, TokenType.IMPORT_BLOCK_CLOSE)
                ));
            }
        } else if (t0 != null) {
            final var diagnostic = new SimpleDiagnostic(
                    getUnexpectedMessage(t0.getType(), TokenType.IMPORT_BLOCK_OPEN)
            );
            this.acc.leaf(t0.getType(), t0.getStartIndex(), t0.getEndIndex(), List.of(diagnostic));
        } else {
            importBlockDiagnostics.add(new SimpleDiagnostic(
                    getNullTokenMessage(TokenType.IMPORT_BLOCK_OPEN)
            ));
        }

        this.acc.done(importBlockDiagnostics);
        this.tokens.advance();
    }

    protected void blockScriptlet() {
        final Collection<Diagnostic> blockScriptletDiagnostics = new ArrayList<>();

        final var t0 = this.tokens.getCurrent();
        if (t0 != null && t0.getType() == TokenType.BLOCK_SCRIPTLET_OPEN) {
            this.tokens.advance();
            blockScriptletDiagnostics.addAll(this.blockExpressionScriptletBodyAndClose());
        } else if (t0 != null) {
            final var diagnostic = new SimpleDiagnostic(
                    getUnexpectedMessage(t0.getType(), TokenType.BLOCK_SCRIPTLET_OPEN)
            );
            this.acc.leaf(t0.getType(), t0.getStartIndex(), t0.getEndIndex(), List.of(diagnostic));
        } else {
            blockScriptletDiagnostics.add(new SimpleDiagnostic(
                    getNullTokenMessage(TokenType.BLOCK_SCRIPTLET_OPEN)
            ));
        }

        this.acc.done(blockScriptletDiagnostics);
        this.tokens.advance();
    }

    private Collection<Diagnostic> blockExpressionScriptletBodyAndClose() {
        final Collection<Diagnostic> diagnostics = new ArrayList<>();

        final var t0 = this.tokens.getCurrent();
        if (t0 != null && t0.getType() == TokenType.SCRIPTLET_BODY) {
            this.tokens.advance();
            final var t1 = this.tokens.getCurrent();
            if (t1 != null && t1.getType() == TokenType.SCRIPTLET_CLOSE) {

            } else if (t1 != null) {

            } else {

            }
        } else if (t0 != null) {

        } else {

        }

        return diagnostics;
    }

    protected void expressionScriptlet() {

    }

    protected void dollarReference() {

    }

    protected void dollarScriptlet() {

    }

}
