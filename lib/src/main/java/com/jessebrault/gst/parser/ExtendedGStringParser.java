package com.jessebrault.gst.parser;

public class ExtendedGStringParser extends StandardGStringParser {

    @Override
    protected boolean isImportBlockPermitted() {
        return true;
    }

}
