package com.jessebrault.gst.tokenizer;

public enum TokenizerState {
    TEXT,
    DOLLAR_REFERENCE_BODY,
    SCRIPTLET_BODY,
    SCRIPTLET_CLOSE
}
