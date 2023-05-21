package com.jessebrault.gst.tokenizer;

public interface Token {

    enum Type {
        TEXT
    }

    Type getType();
    int getStartIndex();
    int getEndIndex();

}
