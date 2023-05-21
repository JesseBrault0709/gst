package com.jessebrault.gst.tokenizer;

public interface Tokenizer {

    enum State {

    }

    void start(CharSequence input, int startIndex, int endIndex, State initialState);
    CharSequence getCurrentInput();
    State getCurrentState();
    Token getCurrentToken();
    void advance();

}
