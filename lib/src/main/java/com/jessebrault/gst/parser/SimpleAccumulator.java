package com.jessebrault.gst.parser;

import com.jessebrault.gst.ast.AstNode;
import com.jessebrault.gst.ast.LeafNode;
import com.jessebrault.gst.ast.TreeNode;
import com.jessebrault.gst.ast.TreeNodeType;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;

import java.util.*;

public final class SimpleAccumulator implements Parser.Accumulator {

    private static final class ParentData {

        private final TreeNodeType type;
        private final Collection<Diagnostic> diagnostics;

        public ParentData(TreeNodeType type, Collection<Diagnostic> diagnostics) {
            this.type = type;
            this.diagnostics = diagnostics;
        }

        public TreeNodeType getType() {
            return this.type;
        }

        public Collection<Diagnostic> getDiagnostics() {
            return this.diagnostics;
        }

    }

    private final Deque<ParentData> parents = new LinkedList<>();
    private final Deque<List<AstNode>> children = new LinkedList<>();

    private AstNode result;

    private List<AstNode> getCurrentChildren() {
        final var currentChildren = this.children.peek();
        if (currentChildren == null) {
            throw new IllegalStateException("currentChildren is null");
        }
        return currentChildren;
    }

    @Override
    public void start(TreeNodeType type, Collection<Diagnostic> diagnostics) {
        this.parents.push(new ParentData(type, diagnostics));
        this.children.push(new ArrayList<>());
    }

    @Override
    public void leaf(TokenType type, int start, int end, Collection<Diagnostic> diagnostics) {
        this.getCurrentChildren().add(new LeafNode(diagnostics, type, start, end));
    }

    @Override
    public void done(Collection<Diagnostic> diagnostics) {
        final var parentData = this.parents.pop();
        final var treeNodeChildren = this.children.pop();
        final var currentChildren = this.children.peek();
        if (currentChildren == null) {
            final Collection<Diagnostic> totalDiagnostics = new ArrayList<>(parentData.getDiagnostics());
            totalDiagnostics.addAll(diagnostics);
            this.result = new TreeNode(totalDiagnostics, parentData.getType(), treeNodeChildren);
        } else {
            currentChildren.add(new TreeNode(diagnostics, parentData.getType(), treeNodeChildren));
        }
    }



    public AstNode getResult() {
        if (this.result == null) {
            throw new IllegalStateException("not done accumulating AST yet");
        }
        return this.result;
    }

}
