package com.jessebrault.gst.ast;

import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;

import java.util.Collection;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(this.diagnostics, this.tokenType, this.tokenStart, this.tokenEnd);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof LeafNode leafNode)) {
            return false;
        } else {
            return this.diagnostics.equals(leafNode.diagnostics)
                    && this.tokenType == leafNode.tokenType
                    && this.tokenStart == leafNode.tokenStart
                    && this.tokenEnd == leafNode.tokenEnd;
        }
    }

    @Override
    public String toString() {
        final var b = new StringBuilder(this.tokenType.toString())
                .append("(").append(this.tokenStart).append(", ").append(this.tokenEnd);
        if (!this.diagnostics.isEmpty()) {
            b.append(", diagnostics: ");
            final var iter = this.diagnostics.iterator();
            while (iter.hasNext()) {
                b.append(iter.next());
                if (iter.hasNext()) {
                    b.append(", ");
                }
            }
        }
        return b.append(")").toString();
    }
}
