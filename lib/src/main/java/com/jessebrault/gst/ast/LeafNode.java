package com.jessebrault.gst.ast;

import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;

import java.util.Collection;

public final class LeafNode implements AstNode {

    private final Collection<Diagnostic> diagnostics;
    private final TokenType tokenType;
    private final int tokenStart;
    private final int tokenEnd;

    public LeafNode(
            Collection<Diagnostic> diagnostics,
            TokenType tokenType,
            int tokenStart,
            int tokenEnd
    ) {
        this.diagnostics = diagnostics;
        this.tokenType = tokenType;
        this.tokenStart = tokenStart;
        this.tokenEnd = tokenEnd;
    }

    @Override
    public Collection<Diagnostic> getDiagnostics() {
        return this.diagnostics;
    }

    public TokenType getTokenType() {
        return this.tokenType;
    }

    public int getTokenStart() {
        return this.tokenStart;
    }

    public int getTokenEnd() {
        return this.tokenEnd;
    }

}
