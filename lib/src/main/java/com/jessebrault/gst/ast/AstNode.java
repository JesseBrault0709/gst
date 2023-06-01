package com.jessebrault.gst.ast;

import com.jessebrault.gst.util.Diagnostic;

import java.util.Collection;

public sealed interface AstNode permits TreeNode, LeafNode {
    Collection<Diagnostic> getDiagnostics();
}
