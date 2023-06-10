package com.jessebrault.gst.parser;

/**
 * May be extended.
 */
public class ExtendedGstParser extends StandardGstParser {

    @Override
    protected boolean isImportBlockPermitted() {
        return true;
    }

    @Override
    public String toString() {
        return "ExtendedGstParser()";
    }

}
