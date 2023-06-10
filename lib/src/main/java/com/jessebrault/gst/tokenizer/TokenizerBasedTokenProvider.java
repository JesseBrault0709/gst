package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class TokenizerBasedTokenProvider implements TokenProvider {

    private final Tokenizer tokenizer;

    public TokenizerBasedTokenProvider(
            Supplier<Tokenizer> tokenizerSupplier,
            CharSequence input,
            int startIndex,
            int endIndex,
            TokenizerState initialState
    ) {
        this.tokenizer = tokenizerSupplier.get();
        this.tokenizer.start(input, startIndex, endIndex, initialState);
    }

    @Override
    public @Nullable TokenType getCurrentType() {
        return this.tokenizer.getCurrentType();
    }

    @Override
    public int getCurrentStart() {
        return this.tokenizer.getCurrentStart();
    }

    @Override
    public int getCurrentEnd() {
        return this.tokenizer.getCurrentEnd();
    }

    @Override
    public void advance() {
        this.tokenizer.advance();
    }

}
