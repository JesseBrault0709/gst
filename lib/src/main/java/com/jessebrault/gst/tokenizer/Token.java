package com.jessebrault.gst.tokenizer;

public interface Token {
    TokenType getType();
    int getStartIndex();
    int getEndIndex();
}
