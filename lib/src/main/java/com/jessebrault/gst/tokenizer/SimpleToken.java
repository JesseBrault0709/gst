package com.jessebrault.gst.tokenizer;

final class SimpleToken implements Token {

    private final Type type;
    private final int startIndex;
    private final int endIndex;

    public SimpleToken(Type type, int startIndex, int endIndex) {
        this.type = type;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public Type getType() {
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

}
