package com.jessebrault.gst.parser;

import com.jessebrault.gst.ast.TreeNodeType;
import com.jessebrault.gst.tokenizer.Token;
import com.jessebrault.gst.tokenizer.TokenProvider;
import com.jessebrault.gst.tokenizer.TokenType;
import com.jessebrault.gst.util.Diagnostic;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

public interface Parser {

    interface Accumulator {

        void start(TreeNodeType type, Collection<Diagnostic> diagnostics);

        void leaf(TokenType type, int start, int end, Collection<Diagnostic> diagnostics);

        void done(Collection<Diagnostic> diagnostics);

        default void start(TreeNodeType type) {
            this.start(type, List.of());
        }

        default void leaf(TokenType type, int start, int end) {
            this.leaf(type, start, end, List.of());
        }
        
        default void done() {
            this.done(List.of());
        }

    }

    void parse(TokenProvider tokenProvider, Accumulator acc);

}
