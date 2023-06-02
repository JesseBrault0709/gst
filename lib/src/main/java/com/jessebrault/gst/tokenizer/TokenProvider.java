package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

public interface TokenProvider {
    TokenType getCurrentType();
    int getCurrentStart();
    int getCurrentEnd();
    void advance();
}
