package com.jessebrault.gst.tokenizer;

import java.util.Objects;

public final class SimpleToken implements Token {

    private final TokenType type;
    private final int startIndex;
    private final int endIndex;

    public SimpleToken(TokenType type, int startIndex, int endIndex) {
        this.type = type;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public TokenType getType() {
        return this.type;
    }

    @Override
    public int getStartIndex() {
        return this.startIndex;
    }

    @Override
    public int getEndIndex() {
        return this.endIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.startIndex, this.endIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Token t)) {
            return false;
        } else {
            return this.type == t.getType()
                    && this.startIndex == t.getStartIndex()
                    && this.endIndex == t.getEndIndex();
        }
    }

    @Override
    public String toString() {
        return "SimpleToken(type: " + this.type
                + ", startIndex: " + this.startIndex
                + ", endIndex: " + this.endIndex
                + ")";
    }

}
