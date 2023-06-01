package com.jessebrault.gst.parser;

import com.jessebrault.gst.tokenizer.TokenProvider;

public interface Parser {
    void parse(TokenProvider tokenProvider, Accumulator acc);
}
