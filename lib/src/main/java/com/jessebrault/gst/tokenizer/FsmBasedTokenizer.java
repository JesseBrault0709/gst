package com.jessebrault.gst.tokenizer;

public final class FsmBasedTokenizer implements Tokenizer {
    
    private CharSequence currentInput;

    @Override
    public void start(CharSequence input, int startIndex, int endIndex, State initialState) {

    }

    @Override
    public CharSequence getCurrentInput() {
        return this.currentInput;
    }

    @Override
    public State getCurrentState() {
        return null;
    }

    @Override
    public Token getCurrentToken() {
        return null;
    }

    @Override
    public void advance() {

    }

}
