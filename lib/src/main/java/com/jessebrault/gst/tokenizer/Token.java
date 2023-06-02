package com.jessebrault.gst.tokenizer;

@Deprecated
public interface Token {
    TokenType getType();
    int getStartIndex();
    int getEndIndex();
}
