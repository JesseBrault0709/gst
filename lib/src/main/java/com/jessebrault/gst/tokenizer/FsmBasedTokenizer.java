package com.jessebrault.gst.tokenizer;

import com.jessebrault.fsm.function.FunctionFsm;
import com.jessebrault.fsm.function.FunctionFsmBuilder;
import com.jessebrault.fsm.function.FunctionFsmBuilderImpl;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.jessebrault.gst.tokenizer.TokenizerState.*;

public final class FsmBasedTokenizer implements Tokenizer {

    private interface FsmFunction extends Function<CharSequence, Integer> {}

    private static final class PatternMatcher implements FsmFunction {

        private final String name;
        private final Pattern pattern;

        public PatternMatcher(String name, String regex) {
            this.name = name;
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public Integer apply(CharSequence input) {
            final var m = this.pattern.matcher(input);
            return m.find() ? m.group().length() : null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.pattern);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof PatternMatcher pm)) {
                return false;
            } else {
                return this.name.equals(pm.name) && this.pattern.equals(pm.pattern);
            }
        }

        @Override
        public String toString() {
            return "PatternMatcher(name: " + this.name + ")";
        }

    }

    private static final FsmFunction text = new PatternMatcher(
            "text",
            "^(?:[\\w\\W&&[^\\$<]]|(?:\\$(?![{a-zA-Z_]))|(?:<(?!%)))+"
    );
    private static final FsmFunction dollarReferenceDollar = new PatternMatcher(
            "dollarReferenceDollar",
            "^\\$(?=[a-zA-Z_$])"
    );
    private static final FsmFunction dollarReferenceBody = new PatternMatcher(
            "dollarReferenceBody",
            "^[a-zA-Z_$][a-zA-Z0-9_$]*(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*"
    );
    private static final FsmFunction blockScriptletOpen = new PatternMatcher(
            "blockScriptletOpen",
            "^<%(?!=)"
    );
    private static final FsmFunction expressionScriptletOpen = new PatternMatcher(
            "expressionScriptletOpen",
            "^<%="
    );

    private static final FsmFunction scriptletBody = new PatternMatcher(
            "scriptletBody",
            "^(?:[\\w\\W&&[^%]]|(?:%(?!>)))+"
    );

    private static final FsmFunction scriptletClose = new PatternMatcher(
            "scriptletClose",
            "^%>"
    );

    private FunctionFsm<CharSequence, TokenizerState, Integer> fsm;

    private CharSequence currentInput;
    private int startIndex;
    private int endIndex;

    private int currentIndex;
    private TokenizerState currentTokenState;
    private Token currentToken;
    private boolean done;

    @Override
    public void start(CharSequence input, int startIndex, int endIndex, TokenizerState initialState) {
        this.done = false;
        this.currentInput = input;
        this.startIndex = startIndex;
        this.currentIndex = this.startIndex;
        this.endIndex = endIndex;
        this.initFsm(initialState);
        this.pullToken();
    }

    @Override
    public CharSequence getCurrentInput() {
        return this.currentInput;
    }

    @Override
    public TokenizerState getCurrentTokenState() {
        return this.currentTokenState;
    }

    @Override
    public int getCurrentStartIndex() {
        return this.startIndex;
    }

    @Override
    public int getCurrentEndIndex() {
        return this.endIndex;
    }

    @Override
    public Token getCurrentToken() {
        return this.done ? null : this.currentToken;
    }

    @Override
    public void advance() {
        if (this.currentToken == null) {
            throw new IllegalStateException("cannot advance; this.currentToken is null");
        }
        this.currentIndex = this.currentToken.getEndIndex();
        this.pullToken();
    }

    private void initFsm(TokenizerState initialState) {
        final FunctionFsmBuilder<CharSequence, TokenizerState, Integer> b = new FunctionFsmBuilderImpl<>();
        this.fsm = b
                .setInitialState(initialState)

                .whileIn(TEXT, sc -> {
                    sc.on(text).exec(length -> this.createCurrentToken(TokenType.TEXT, length));
                    sc.on(dollarReferenceDollar).shiftTo(DOLLAR_REFERENCE_BODY).exec(length ->
                            this.createCurrentToken(TokenType.DOLLAR_REFERENCE_DOLLAR, length)
                    );
                    sc.on(blockScriptletOpen).shiftTo(SCRIPTLET_BODY).exec(length ->
                            this.createCurrentToken(TokenType.BLOCK_SCRIPTLET_OPEN, length)
                    );
                    sc.on(expressionScriptletOpen).shiftTo(SCRIPTLET_BODY).exec(length ->
                            this.createCurrentToken(TokenType.EXPRESSION_SCRIPTLET_OPEN, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .whileIn(DOLLAR_REFERENCE_BODY, sc -> {
                    sc.on(dollarReferenceBody).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.DOLLAR_REFERENCE_BODY, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .whileIn(SCRIPTLET_BODY, sc -> {
                    sc.on(scriptletBody).shiftTo(SCRIPTLET_CLOSE).exec(length ->
                            this.createCurrentToken(TokenType.SCRIPTLET_BODY, length)
                    );
                    sc.on(scriptletClose).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.SCRIPTLET_CLOSE, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .whileIn(SCRIPTLET_CLOSE, sc -> {
                    sc.on(scriptletClose).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.SCRIPTLET_CLOSE, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .build();
    }

    private void createCurrentToken(TokenType type, int length) {
        this.currentToken = new SimpleToken(type, this.currentIndex, this.currentIndex + length);
    }

    private void done(CharSequence input) {
        this.done = true;
    }

    private void pullToken() {
        this.currentTokenState = this.fsm.getCurrentState();
        this.fsm.apply(
                this.currentInput.subSequence(
                        Math.max(this.currentIndex, this.startIndex),
                        this.endIndex
                )
        );
    }

}
