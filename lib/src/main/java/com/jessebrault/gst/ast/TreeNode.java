package com.jessebrault.gst.ast;

import com.jessebrault.gst.util.Diagnostic;

import java.util.Collection;
import java.util.List;

public final class TreeNode implements AstNode {

    private final Collection<Diagnostic> diagnostics;
    private final TreeNodeType type;
    private final List<AstNode> children;

    public TreeNode(Collection<Diagnostic> diagnostics, TreeNodeType type, List<AstNode> children) {
        this.diagnostics = diagnostics;
        this.type = type;
        this.children = children;
    }

    @Override
    public Collection<Diagnostic> getDiagnostics() {
        return this.diagnostics;
    }

    public TreeNodeType getType() {
        return this.type;
    }

    public List<AstNode> getChildren() {
        return this.children;
    }

}
