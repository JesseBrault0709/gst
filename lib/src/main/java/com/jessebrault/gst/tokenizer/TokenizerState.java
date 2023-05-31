package com.jessebrault.gst.tokenizer;

public enum TokenizerState {
    TEXT,
    DOLLAR_REFERENCE_BODY,
    SCRIPTLET_BODY,
    SCRIPTLET_CLOSE,
    IMPORT_BLOCK_BODY,
    IMPORT_BLOCK_CLOSE,
    DOLLAR_SCRIPTLET_BODY,
    DOLLAR_SCRIPTLET_CLOSE
}
