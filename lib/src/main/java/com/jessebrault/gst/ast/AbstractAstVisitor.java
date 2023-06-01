package com.jessebrault.gst.ast;

public abstract class AbstractAstVisitor implements AstVisitor {

    protected final void visitChildren(TreeNode node) {
        node.getChildren().forEach(child -> {
            if (child instanceof TreeNode treeNodeChild) {
                switch (treeNodeChild.getType()) {
                    case G_STRING -> this.visitGString(treeNodeChild);
                    case DOLLAR_REFERENCE -> this.visitDollarReference(treeNodeChild);
                    case BLOCK_SCRIPTLET -> this.visitBlockScriptlet(treeNodeChild);
                    case EXPRESSION_SCRIPTLET -> this.visitExpressionScriptlet(treeNodeChild);
                    case IMPORT_BLOCK -> this.visitImportBlock(treeNodeChild);
                    case DOLLAR_SCRIPTLET -> this.visitDollarScriptlet(treeNodeChild);
                }
            } else if (child instanceof LeafNode leafNodeChild) {
                switch (leafNodeChild.getTokenType()) {
                    case TEXT -> this.visitText(leafNodeChild);
                    case DOLLAR_REFERENCE_DOLLAR -> this.visitDollarReferenceDollar(leafNodeChild);
                    case DOLLAR_REFERENCE_BODY -> this.visitDollarReferenceBody(leafNodeChild);
                    case BLOCK_SCRIPTLET_OPEN -> this.visitBlockScriptletOpen(leafNodeChild);
                    case EXPRESSION_SCRIPTLET_OPEN -> this.visitExpressionScriptletOpen(leafNodeChild);
                    case SCRIPTLET_BODY -> this.visitScriptletBody(leafNodeChild);
                    case SCRIPTLET_CLOSE -> this.visitScriptletClose(leafNodeChild);
                    case IMPORT_BLOCK_OPEN -> this.visitImportBlockOpen(leafNodeChild);
                    case IMPORT_BLOCK_BODY -> this.visitImportBlockBody(leafNodeChild);
                    case IMPORT_BLOCK_CLOSE -> this.visitImportBlockClose(leafNodeChild);
                    case DOLLAR_SCRIPTLET_OPEN -> this.visitDollarScriptletOpen(leafNodeChild);
                    case DOLLAR_SCRIPTLET_BODY -> this.visitDollarScriptletBody(leafNodeChild);
                    case DOLLAR_SCRIPTLET_CLOSE -> this.visitDollarScriptletClose(leafNodeChild);
                }
            }
        });
    }

    @Override
    public void defaultBefore(AstNode astNode) {}

    @Override
    public void defaultAfter(AstNode after) {}

    @Override
    public void visitGString(TreeNode node) {
        this.defaultBefore(node);
        this.visitChildren(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarReference(TreeNode node) {
        this.defaultBefore(node);
        this.visitChildren(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitBlockScriptlet(TreeNode node) {
        this.defaultBefore(node);
        this.visitChildren(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitExpressionScriptlet(TreeNode node) {
        this.defaultBefore(node);
        this.visitChildren(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitImportBlock(TreeNode node) {
        this.defaultBefore(node);
        this.visitChildren(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarScriptlet(TreeNode node) {
        this.defaultBefore(node);
        this.visitChildren(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitText(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarReferenceDollar(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarReferenceBody(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitBlockScriptletOpen(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitExpressionScriptletOpen(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitScriptletBody(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitScriptletClose(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitImportBlockOpen(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitImportBlockBody(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitImportBlockClose(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarScriptletOpen(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarScriptletBody(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

    @Override
    public void visitDollarScriptletClose(LeafNode node) {
        this.defaultBefore(node);
        this.defaultAfter(node);
    }

}
