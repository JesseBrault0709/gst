package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

public interface TokenProvider {
    @Nullable TokenType getCurrentType();
    int getCurrentStart();
    int getCurrentEnd();
    void advance();
}
