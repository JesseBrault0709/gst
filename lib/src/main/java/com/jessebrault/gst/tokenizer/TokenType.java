package com.jessebrault.gst.tokenizer;

public enum TokenType {
    TEXT,
    DOLLAR_REFERENCE,

    BLOCK_SCRIPTLET_OPEN,
    EXPRESSION_SCRIPTLET_OPEN,
    SCRIPTLET_BODY,
    SCRIPTLET_CLOSE,

    DOLLAR_SCRIPTLET_OPEN,
    DOLLAR_SCRIPTLET_BODY,
    DOLLAR_SCRIPTLET_CLOSE
}