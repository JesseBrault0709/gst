package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ListBasedTokenProvider implements TokenProvider {

    private final List<Token> tokens;
    private int position;

    public ListBasedTokenProvider(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public @Nullable Token getCurrent() {
        if (this.position >= this.tokens.size()) {
            return null;
        } else {
            return this.tokens.get(this.position);
        }
    }

    @Override
    public void advance() {
        this.position++;
    }

}
