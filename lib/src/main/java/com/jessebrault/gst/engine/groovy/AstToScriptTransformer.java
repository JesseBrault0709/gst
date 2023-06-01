package com.jessebrault.gst.engine.groovy;

import com.jessebrault.gst.ast.*;
import com.jessebrault.gst.parser.SimpleAccumulator;
import com.jessebrault.gst.parser.StandardGstParser;
import com.jessebrault.gst.tokenizer.FsmBasedTokenizer;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.tokenizer.TokenizerBasedTokenProvider;

import java.util.Collection;
import java.util.List;

public final class AstToScriptTransformer extends AbstractAstVisitor {

    // Testing only
    public static void main(String[] args) {
        final var tokenizer = new FsmBasedTokenizer();
        final var acc = new SimpleAccumulator();
        final var parser = new StandardGstParser();
        final var input = "<%= unfinished() %>";
        parser.parse(new TokenizerBasedTokenProvider(input, tokenizer), acc);
        final var root = acc.getResult();

        final var prettyPrinter = new AstPrettyPrinterVisitor();
        prettyPrinter.visitGString(root);
        System.out.println("\n" + prettyPrinter.getResult() + "\n");

        if (!AstUtil.hasDiagnostics(root)) {
            final var v = new AstToScriptTransformer(List.of(), input);
            if (root.getType() == TreeNodeType.G_STRING) {
                v.visitGString(root);
            }
            System.out.println("\n" + v.getResult() + "\n");
        }
    }

    private final Collection<String> importStatements;
    private final CharSequence text;

    private final StringBuilder b = new StringBuilder();
    private int indentIndex = 0;

    public AstToScriptTransformer(Collection<String> importStatements, CharSequence text) {
        this.importStatements = importStatements;
        this.text = text;
    }

    public String getResult() {
        return this.b.toString();
    }

    @SuppressWarnings("SameParameterValue")
    private void write(String text, boolean indent) {
        if (indent) {
            this.b.append("    ".repeat(this.indentIndex));
        }
        this.b.append(text);
    }

    private void writeLine(String text, boolean indent) {
        if (indent) {
            this.b.append("    ".repeat(this.indentIndex));
        }
        this.b.append(text);
        this.b.append("\n");
    }

    private void writeLine(String text) {
        this.writeLine(text, false);
    }

    private void writeLeafNode(LeafNode leafNode) {
        this.b.append(this.text.subSequence(leafNode.getTokenStart(), leafNode.getTokenEnd()));
    }

    private void writeLeafNodeLine(LeafNode leafNode) {
        this.b.append(this.text.subSequence(leafNode.getTokenStart(), leafNode.getTokenEnd())).append("\n");
    }

    @Override
    public void visitGString(TreeNode node) {
        this.writeLine("package com.jessebrault.gst.tmp");
        this.writeLine(""); // extra line
        this.importStatements.forEach(this::writeLine);
        final var importBlocks = AstUtil.findDescendantImportBlocks(node);
        importBlocks.forEach(importBlock -> {
            final var body = AstUtil.getFirstChildLeafWithType(importBlock, TokenType.IMPORT_BLOCK_BODY);
            if (body != null) {
                this.writeLeafNodeLine(body);
            }
        });
        this.writeLine("def getTemplate() {");
        this.indentIndex++;
        this.writeLine("return { out ->", true);
        this.indentIndex++;
        super.visitGString(node);
        this.indentIndex--;
        this.writeLine("}", true);
        this.indentIndex--;
        if (this.indentIndex != 0) {
            throw new IllegalStateException("this.indentIndex is " + this.indentIndex);
        }
        this.writeLine("}");
    }

    @Override
    public void visitBlockScriptlet(TreeNode node) {
        final var body = AstUtil.getFirstChildLeafWithType(node, TokenType.SCRIPTLET_BODY);
        if (body != null) {
            this.writeLeafNodeLine(body);
        }
    }

    @Override
    public void visitExpressionScriptlet(TreeNode node) {
        final var body = AstUtil.getFirstChildLeafWithType(node, TokenType.SCRIPTLET_BODY);
        if (body != null) {
            this.writeLine("out << {", true);
            this.indentIndex++;
            this.writeLeafNode(body);
            this.indentIndex--;
            this.write("\n", false);
            this.writeLine("}()", true);
        }
    }

    @Override
    public void visitDollarScriptlet(TreeNode node) {
        final var body = AstUtil.getFirstChildLeafWithType(node, TokenType.DOLLAR_SCRIPTLET_BODY);
        if (body != null) {
            this.writeLine("out << {", true);
            this.indentIndex++;
            this.writeLeafNode(body);
            this.indentIndex--;
            this.write("\n", false);
            this.writeLine("}()", true);
        }
    }

    @Override
    public void visitText(LeafNode node) {
        this.write("out << \"\"\"", true);
        this.writeLeafNode(node);
        this.writeLine("\"\"\"");
    }

    @Override
    public void visitDollarReferenceBody(LeafNode node) {
        this.write("out << ", true);
        this.writeLeafNode(node);
        this.b.append("\n");
    }

}
