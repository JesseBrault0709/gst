package com.jessebrault.gst.parser;

import com.jessebrault.gst.ast.AstNode;
import com.jessebrault.gst.ast.LeafNode;
import com.jessebrault.gst.ast.TreeNode;
import com.jessebrault.gst.ast.TreeNodeType;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;

import java.util.*;

public final class SimpleAccumulator implements Accumulator {

    private final Deque<TreeNodeType> parents = new LinkedList<>();
    private final Deque<List<AstNode>> children = new LinkedList<>();

    private TreeNode result;

    private List<AstNode> getCurrentChildren() {
        final var currentChildren = this.children.peek();
        if (currentChildren == null) {
            throw new IllegalStateException("currentChildren is null");
        }
        return currentChildren;
    }

    @Override
    public void start(TreeNodeType type) {
        this.parents.push(type);
        this.children.push(new ArrayList<>());
    }

    @Override
    public void leaf(TokenType type, int start, int end, Collection<Diagnostic> diagnostics) {
        this.getCurrentChildren().add(new LeafNode(diagnostics, type, start, end));
    }

    @Override
    public void done(Collection<Diagnostic> diagnostics) {
        final var parentType = this.parents.pop();
        final var treeNodeChildren = this.children.pop();
        final var currentChildren = this.children.peek();
        if (currentChildren == null) {
            this.result = new TreeNode(diagnostics, parentType, treeNodeChildren);
        } else {
            currentChildren.add(new TreeNode(diagnostics, parentType, treeNodeChildren));
        }
    }

    public TreeNode getResult() {
        if (this.result == null) {
            throw new IllegalStateException("not done accumulating AST yet");
        }
        return this.result;
    }

}
