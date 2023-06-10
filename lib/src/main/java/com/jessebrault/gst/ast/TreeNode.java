package com.jessebrault.gst.ast;

import com.jessebrault.gst.util.Diagnostic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(this.diagnostics, this.type, this.children);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TreeNode treeNode)) {
            return false;
        } else {
            return this.diagnostics.equals(treeNode.diagnostics)
                    && this.type == treeNode.type
                    && this.children.equals(treeNode.children);
        }
    }

    @Override
    public String toString() {
        final var b = new StringBuilder(this.type.toString());
        if (!this.diagnostics.isEmpty()) {
            b.append("(diagnostics: ");
            final var iter = this.diagnostics.iterator();
            while (iter.hasNext()) {
                b.append(iter.next());
                if (iter.hasNext()) {
                    b.append(", ");
                }
            }
            b.append(")");
        }
        return b.toString();
    }

}
