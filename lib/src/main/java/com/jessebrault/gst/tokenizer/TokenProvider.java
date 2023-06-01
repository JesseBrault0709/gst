package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

public interface TokenProvider {
    @Nullable Token getCurrent();
    boolean peekCurrent(TokenType type);
    boolean peekSecond(TokenType type);
    boolean peekInfinite(TokenType type, TokenType... failOn);
    void advance();
}
