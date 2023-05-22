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
    private static final FsmFunction dollarReference = new PatternMatcher(
            "dollarReference",
            "^\\$[a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*"
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
            "^.+?(?=%>)"
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

    @Override
    public void start(CharSequence input, int startIndex, int endIndex, TokenizerState initialState) {
        this.currentInput = input;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.initFsm(initialState);
        this.pullToken();
    }

    @Override
    public CharSequence getCurrentInput() {
        return this.currentInput;
    }

    @Override
    public TokenizerState getCurrentState() {
        return this.currentTokenState;
    }

    @Override
    public Token getCurrentToken() {
        return this.currentToken;
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
                    sc.on(dollarReference).exec(length -> this.createCurrentToken(TokenType.DOLLAR_REFERENCE, length));
                    sc.on(blockScriptletOpen).shiftTo(SCRIPTLET_BODY).exec(length ->
                            this.createCurrentToken(TokenType.BLOCK_SCRIPTLET_OPEN, length)
                    );
                    sc.on(expressionScriptletOpen).shiftTo(SCRIPTLET_BODY).exec(length ->
                            this.createCurrentToken(TokenType.EXPRESSION_SCRIPTLET_OPEN, length)
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
                })

                .build();
    }

    private void createCurrentToken(TokenType type, int length) {
        this.currentToken = new SimpleToken(type, this.currentIndex, this.currentIndex + length);
    }

    private void done(CharSequence input) {
        this.currentToken = null;
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
