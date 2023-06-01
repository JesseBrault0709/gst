package com.jessebrault.gst.parser

import com.jessebrault.gst.ast.TreeNodeType.IMPORT_BLOCK
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StandardGstParserTests : AbstractParserTests(StandardGstParser()) {

    override val isImportBlockLegal: Boolean = false

    @Test
    fun importBlockIsIllegal() {
        this.doGStringTest("<%@%>") {
            hasChild(IMPORT_BLOCK) {
                hasDiagnostics {
                    assertEquals(1, it.size)
                }
            }
        }
    }

}
