package com.jessebrault.gst.ast;

import org.jetbrains.annotations.Nullable;

public final class AstUtil {

    public static @Nullable AstNode getLastChild(TreeNode node) {
        final var size = node.getChildren().size();
        if (size > 0) {
            return node.getChildren().get(size);
        } else {
            return null;
        }
    }

    private AstUtil() {}

}
