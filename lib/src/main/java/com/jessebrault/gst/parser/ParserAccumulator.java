package com.jessebrault.gst.parser;

import com.jessebrault.gst.ast.TreeNodeType;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;

import java.util.Collection;
import java.util.List;

public interface ParserAccumulator {

    void start(TreeNodeType type);

    void leaf(TokenType type, int start, int end, Collection<Diagnostic> diagnostics);

    void done(Collection<Diagnostic> diagnostics);

    default void leaf(TokenType type, int start, int end) {
        this.leaf(type, start, end, List.of());
    }

    default void done() {
        this.done(List.of());
    }

}
