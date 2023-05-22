package com.jessebrault.gst.tokenizer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface Tokenizer {

    void start(CharSequence input, int startIndex, int endIndex, TokenizerState initialState);

    CharSequence getCurrentInput();
    TokenizerState getCurrentTokenState();

    int getCurrentStartIndex();
    int getCurrentEndIndex();

    @Nullable Token getCurrentToken();

    void advance();

    default List<Token> tokenize(CharSequence input, int startIndex, int endIndex, TokenizerState initialState) {
        this.start(input, startIndex, endIndex, initialState);
        final List<Token> tokens = new ArrayList<>();
        Token t;
        while ((t = this.getCurrentToken()) != null) {
            tokens.add(t);
            this.advance();
        }
        return tokens;
    }

    default List<Token> tokenize(CharSequence input) {
        return this.tokenize(input, 0, input.length(), TokenizerState.TEXT);
    }

}
