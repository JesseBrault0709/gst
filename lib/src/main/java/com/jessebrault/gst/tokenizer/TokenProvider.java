package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

public interface TokenProvider {
    @Nullable Token getCurrent();
    void advance();
}
