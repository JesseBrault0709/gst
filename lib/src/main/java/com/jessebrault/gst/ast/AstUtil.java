package com.jessebrault.gst.ast;

import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public final class AstUtil {

    public static Collection<TreeNode> findDescendantImportBlocks(TreeNode node) {
        final Collection<TreeNode> importBlocks = new ArrayList<>();
        for (final AstNode child : node.getChildren()) {
            if (child instanceof TreeNode treeNodeChild) {
                if (treeNodeChild.getType() == TreeNodeType.IMPORT_BLOCK) {
                    importBlocks.add(treeNodeChild);
                } else {
                    importBlocks.addAll(findDescendantImportBlocks(treeNodeChild));
                }
            }
        }
        return importBlocks;
    }

    public static @Nullable LeafNode getFirstChildLeafWithType(TreeNode parent, TokenType childType) {
        for (final AstNode child : parent.getChildren()) {
            if (child instanceof LeafNode leafNodeChild && leafNodeChild.getTokenType() == childType) {
                return leafNodeChild;
            }
        }
        return null;
    }

    public static Collection<Diagnostic> getAllDiagnostics(AstNode node) {
        final Collection<Diagnostic> diagnostics = new ArrayList<>(node.getDiagnostics());
        if (node instanceof TreeNode treeNode) {
            for (final AstNode child : treeNode.getChildren()) {
                diagnostics.addAll(getAllDiagnostics(child));
            }
        }
        return diagnostics;
    }

    public static boolean hasDiagnostics(AstNode node) {
        return !getAllDiagnostics(node).isEmpty();
    }

    private AstUtil() {}

}
