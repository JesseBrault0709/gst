package com.jessebrault.gst.tokenizer;

public interface Tokenizer extends TokenProvider {
    void start(CharSequence input, int startIndex, int endIndex, TokenizerState initialState);

    CharSequence getCurrentInput();
    TokenizerState getCurrentTokenState();

    int getInputStartIndex();
    int getInputEndIndex();
}
