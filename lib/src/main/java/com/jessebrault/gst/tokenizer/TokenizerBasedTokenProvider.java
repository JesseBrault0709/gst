package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

public final class TokenizerBasedTokenProvider implements TokenProvider {

    private final Tokenizer tokenizer;

    public TokenizerBasedTokenProvider(CharSequence input, Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.tokenizer.start(input, 0, input.length(), TokenizerState.TEXT);
    }

    @Override
    public @Nullable Token getCurrent() {
        return this.tokenizer.getCurrentToken();
    }

    @Override
    public void advance() {
        this.tokenizer.advance();
    }

}
