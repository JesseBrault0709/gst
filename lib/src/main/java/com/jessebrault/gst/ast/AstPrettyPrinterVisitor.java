package com.jessebrault.gst.ast;

public final class AstPrettyPrinterVisitor extends AbstractAstVisitor {

    private final StringBuilder b = new StringBuilder();

    private int indentIndex;

    public String getResult() {
        return this.b.toString();
    }

    @Override
    protected void defaultBefore(AstNode astNode) {
        this.b.append("  ".repeat(this.indentIndex)).append(astNode).append("\n");
        this.indentIndex++;
    }

    @Override
    protected void defaultAfter(AstNode after) {
        this.indentIndex--;
    }

}
