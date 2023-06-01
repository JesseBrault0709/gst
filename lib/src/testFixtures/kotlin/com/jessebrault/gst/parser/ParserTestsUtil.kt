package com.jessebrault.gst.parser

import com.jessebrault.gst.ast.AstNode
import com.jessebrault.gst.ast.LeafNode
import com.jessebrault.gst.ast.TreeNode
import com.jessebrault.gst.ast.TreeNodeType
import com.jessebrault.gst.tokenizer.TokenType
import com.jessebrault.gst.util.Diagnostic
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

fun assertGString(node: AstNode, tests: NodeTester.() -> Unit) {
    tests(NodeTester(node))
}

class NodeTester(private val node: AstNode) {

    private var current: Int = 0

    fun hasChild(treeNodeType: TreeNodeType, tests: NodeTester.() -> Unit) {
        assertIs<TreeNode>(this.node)
        val child = this.node.children[this.current]
        assertIs<TreeNode>(child)
        assertEquals(treeNodeType, child.type)
        tests(NodeTester(child))
        this.current++
    }

    fun hasChild(tokenType: TokenType, tokenStart: Int? = null, tokenEnd: Int? = null) {
        assertIs<TreeNode>(this.node)
        val child = this.node.children[this.current]
        assertIs<LeafNode>(child)
        assertEquals(tokenType, child.tokenType)
        tokenStart?.let {
            assertEquals(it, child.tokenStart)
        }
        tokenEnd?.let {
            assertEquals(it, child.tokenEnd)
        }
        this.current++
    }

    fun hasNoDiagnostics() {
        assertTrue(this.node.diagnostics.isEmpty())
    }

    fun hasDiagnostics(tests: ((Collection<Diagnostic>) -> Unit)? = null) {
        assertTrue(!this.node.diagnostics.isEmpty())
        tests?.let {
            it(this.node.diagnostics)
        }
    }

}
