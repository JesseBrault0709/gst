package com.jessebrault.gst.parser

class ExtendedGstParserTests : AbstractParserTests(ExtendedGstParser()) {
    override val isImportBlockLegal: Boolean = true
}
