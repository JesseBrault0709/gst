package com.jessebrault.gst.ast;

public interface AstVisitor {
    void visitGString(TreeNode node);
    void visitDollarReference(TreeNode node);
    void visitBlockScriptlet(TreeNode node);
    void visitExpressionScriptlet(TreeNode node);
    void visitImportBlock(TreeNode node);
    void visitDollarScriptlet(TreeNode node);

    void visitText(LeafNode node);
    void visitDollarReferenceDollar(LeafNode node);
    void visitDollarReferenceBody(LeafNode node);
    void visitBlockScriptletOpen(LeafNode node);
    void visitExpressionScriptletOpen(LeafNode node);
    void visitScriptletBody(LeafNode node);
    void visitScriptletClose(LeafNode node);
    void visitImportBlockOpen(LeafNode node);
    void visitImportBlockBody(LeafNode node);
    void visitImportBlockClose(LeafNode node);
    void visitDollarScriptletOpen(LeafNode node);
    void visitDollarScriptletBody(LeafNode node);
    void visitDollarScriptletClose(LeafNode node);
}
