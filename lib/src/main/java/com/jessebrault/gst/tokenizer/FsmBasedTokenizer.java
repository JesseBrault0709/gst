package com.jessebrault.gst.tokenizer;

import com.jessebrault.fsm.function.FunctionFsm;
import com.jessebrault.fsm.function.FunctionFsmBuilder;
import com.jessebrault.fsm.function.FunctionFsmBuilderImpl;
import com.jessebrault.fsm.stackfunction.StackFunctionFsm;
import com.jessebrault.fsm.stackfunction.StackFunctionFsmBuilder;
import com.jessebrault.fsm.stackfunction.StackFunctionFsmBuilderImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.jessebrault.gst.tokenizer.TokenizerState.*;

public final class FsmBasedTokenizer implements Tokenizer {

    private interface FsmFunction extends Function<CharSequence, Integer> {
        FsmFunction filter(UnaryOperator<Integer> filter);
    }

    private static abstract class AbstractFsmFunction implements FsmFunction {

        private final String name;

        public AbstractFsmFunction(String name) {
            this.name = name;
        }

        @Override
        public final FsmFunction filter(UnaryOperator<Integer> filter) {
            return new AbstractFsmFunction(this.name) {

                @Override
                public Integer apply(CharSequence charSequence) {
                    return filter.apply(AbstractFsmFunction.this.apply(charSequence));
                }

            };
        }

        @Override
        public String toString() {
            return "FsmFunction(" + this.name + ")";
        }

    }

    private static final class PatternMatcher extends AbstractFsmFunction {

        private final Pattern pattern;

        public PatternMatcher(String name, String regex) {
            super(name);
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public Integer apply(CharSequence input) {
            final var m = this.pattern.matcher(input);
            return m.find() ? m.group().length() : null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.pattern);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof PatternMatcher pm)) {
                return false;
            } else {
                return this.pattern.equals(pm.pattern);
            }
        }

    }

    private static final class DollarScriptletBodyMatcher extends AbstractFsmFunction {

        private static final FsmFunction dollarScriptletText = new PatternMatcher(
                "dollarScriptletText",
                "^[\\w\\W&&[^{}\"']]+"
        );
        private static final FsmFunction leftBrace = new PatternMatcher(
                "leftBrace",
                "^\\{"
        );
        private static final FsmFunction rightBrace = new PatternMatcher(
                "rightBrace",
                "^}"
        );
        private static final FsmFunction doubleQuote = new PatternMatcher(
                "doubleQuote",
                "^\""
        );
        private static final FsmFunction singleQuote = new PatternMatcher(
                "singleQuote",
                "^'"
        );
        private static final FsmFunction gStringText = new PatternMatcher(
                "gStringText",
                "^(?:[\\w\\W&&[^$\\\"]]|\\$(?!\\{)|(?<=\\\\)\\\")+"
        );
        private static final FsmFunction jStringText = new PatternMatcher(
                "jStringText",
                "^(?:[\\w\\W&&[^']]|(?<=\\\\)')+"
        );
        private static final FsmFunction gStringClosureOpen = new PatternMatcher(
                "gStringClosureOpen",
                "^\\$\\{"
        );

        private enum State {
            ROOT,
            INNER,
            IN_G_STRING,
            IN_J_STRING,
            DONE
        }

        private static StackFunctionFsmBuilder<CharSequence, State, Integer> getFsmBuilder() {
            return new StackFunctionFsmBuilderImpl<>();
        }

        private static StackFunctionFsm<CharSequence, State, Integer> getFsm(AtomicInteger lengthCounter) {
            final Deque<AtomicInteger> counterStack = new LinkedList<>();
            final Supplier<AtomicInteger> currentCounterSupplier = () -> {
                final var currentCounter = counterStack.peek();
                if (currentCounter == null) {
                    throw new IllegalStateException("currentCounter is null");
                }
                return currentCounter;
            };

            final var nonFinalRightBrace = rightBrace.filter(length ->
                    currentCounterSupplier.get().get() > 1 ? length : null
            );

            final var finalRightBrace = rightBrace.filter(length ->
                    currentCounterSupplier.get().get() == 1 ? length : null
            );

            // init counterStack
            final var rootCounter = new AtomicInteger();
            rootCounter.set(1);
            counterStack.push(rootCounter);

            return getFsmBuilder()

                    .setInitialState(State.ROOT)

                    .whileIn(State.ROOT, sc -> {
                        sc.on(dollarScriptletText).exec(lengthCounter::addAndGet);
                        sc.on(leftBrace).exec(length -> {
                            lengthCounter.addAndGet(length);
                            if (counterStack.size() != 1) {
                                throw new IllegalStateException("counterStack.size() is " + counterStack.size());
                            }
                            final var currentCount = currentCounterSupplier.get().incrementAndGet();
                            if (currentCount <= 1) {
                                throw new IllegalStateException("currentCount is " + currentCount);
                            }
                        });
                        sc.on(nonFinalRightBrace).exec(length -> {
                            lengthCounter.addAndGet(length);
                            if (counterStack.size() != 1) {
                                throw new IllegalStateException("counterStack.size() is " + counterStack.size());
                            }
                            final var currentCount = currentCounterSupplier.get().decrementAndGet();
                            if (currentCount < 1) {
                                throw new IllegalStateException("currentCount is " + currentCount);
                            }
                        });
                        // shift
                        sc.on(finalRightBrace).shiftTo(State.DONE).exec(length -> {
                            // DO NOT ADD THE LENGTH:
                            // the final "}" is accounted for in the outer fsm.
                            final var finalCount = currentCounterSupplier.get().decrementAndGet();
                            if (finalCount != 0) {
                                throw new IllegalStateException("finalCount is " + finalCount);
                            }
                        });
                        sc.on(doubleQuote).pushState(State.IN_G_STRING).exec(lengthCounter::addAndGet);
                        sc.on(singleQuote).pushState(State.IN_J_STRING).exec(lengthCounter::addAndGet);
                        sc.onNoMatch().shiftTo(State.DONE);
                    })

                    .whileIn(State.INNER, sc -> {
                        sc.on(dollarScriptletText).exec(lengthCounter::addAndGet);
                        sc.on(leftBrace).exec(length -> {
                            lengthCounter.addAndGet(length);
                            final var currentCount = currentCounterSupplier.get().incrementAndGet();
                            if (currentCount <= 1) {
                                throw new IllegalStateException("currentCount is " + currentCount);
                            }
                        });
                        sc.on(nonFinalRightBrace).exec(length -> {
                            lengthCounter.addAndGet(length);
                            final var currentCount = currentCounterSupplier.get().decrementAndGet();
                            if (currentCount < 1) {
                                throw new IllegalStateException("currentCount is " + currentCount);
                            }
                        });
                        // pop
                        sc.on(finalRightBrace).popState().exec(length -> {
                            lengthCounter.addAndGet(length);
                            final var finalCount = currentCounterSupplier.get().decrementAndGet();
                            if (finalCount != 0) {
                                throw new IllegalStateException("finalCount is " + finalCount);
                            }
                            counterStack.pop();
                            if (counterStack.size() == 0) {
                                throw new IllegalStateException("counterStack.size() is 0");
                            }
                        });
                        sc.on(doubleQuote).pushState(State.IN_G_STRING).exec(lengthCounter::addAndGet);
                        sc.on(singleQuote).pushState(State.IN_J_STRING).exec(lengthCounter::addAndGet);
                        sc.onNoMatch().shiftTo(State.DONE);
                    })

                    .whileIn(State.IN_G_STRING, sc -> {
                        sc.on(gStringText).exec(lengthCounter::addAndGet);
                        sc.on(doubleQuote).popState().exec(lengthCounter::addAndGet);
                        sc.on(gStringClosureOpen).pushState(State.INNER).exec(length -> {
                            lengthCounter.addAndGet(length);
                            counterStack.push(new AtomicInteger());
                            currentCounterSupplier.get().incrementAndGet();
                        });
                        sc.onNoMatch().shiftTo(State.DONE);
                    })

                    .whileIn(State.IN_J_STRING, sc -> {
                        sc.on(jStringText).exec(lengthCounter::addAndGet);
                        sc.on(singleQuote).popState().exec(lengthCounter::addAndGet);
                        sc.onNoMatch().shiftTo(State.DONE);
                    })

                    .build();
        }

        public DollarScriptletBodyMatcher() {
            super("dollarScriptletBodyMatcher");
        }

        @Override
        public Integer apply(final CharSequence input) {
            final var lengthCounter = new AtomicInteger();
            final var fsm = getFsm(lengthCounter);
            CharSequence remaining = input;
            while (remaining.length() > 0) {
                final var outputLength = fsm.apply(remaining);
                if (outputLength == null) {
                    throw new IllegalStateException("outputLength is null");
                }
                if (fsm.getCurrentState() == State.DONE) {
                    break;
                } else {
                    remaining = remaining.subSequence(outputLength, remaining.length());
                }
            }
            final var length = lengthCounter.get();
            if (length > 0) {
                return length;
            } else {
                return null;
            }
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
            "^<%(?![=@])"
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

    private static final FsmFunction importBlockOpen = new PatternMatcher(
            "importBlockOpen",
            "^<%@"
    );
    private static final FsmFunction importBlockBody = new PatternMatcher(
            "importBlockBody",
            "^(?:[\\w\\W&&[^%]]|(?:%(?!>)))+"
    );
    private static final FsmFunction importBlockClose = new PatternMatcher(
            "importBlockClose",
            "^%>"
    );

    private static final FsmFunction dollarScriptletOpen = new PatternMatcher(
            "dollarScriptletOpen",
            "^\\$\\{"
    );
    private static final FsmFunction dollarScriptletBody = new DollarScriptletBodyMatcher();
    private static final FsmFunction dollarScriptletClose = new PatternMatcher(
            "dollarScriptletClose",
            "^}"
    );

    private FunctionFsm<CharSequence, TokenizerState, Integer> fsm;

    private CharSequence currentInput;
    private int inputStartIndex;
    private int inputEndIndex;
    private int currentIndex;
    private TokenizerState currentTokenState;

    private TokenType currentTokenType;
    private int currentTokenStart;
    private int currentTokenEnd;

    private boolean done;

    @Override
    public void start(CharSequence input, int startIndex, int endIndex, TokenizerState initialState) {
        this.done = false;
        this.currentInput = input;
        this.inputStartIndex = startIndex;
        this.currentIndex = this.inputStartIndex;
        this.inputEndIndex = endIndex;
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
    public int getInputStartIndex() {
        return this.inputStartIndex;
    }

    @Override
    public int getInputEndIndex() {
        return this.inputEndIndex;
    }

    @Override
    public @Nullable TokenType getCurrentType() {
        return this.done ? null : this.currentTokenType;
    }

    @Override
    public int getCurrentStart() {
        return this.currentTokenStart;
    }

    @Override
    public int getCurrentEnd() {
        return this.currentTokenEnd;
    }

    @Override
    public void advance() {
        if (this.currentTokenType == null) {
            throw new IllegalStateException("cannot advance; this.currentTokenType is null");
        }
        this.currentIndex = this.currentTokenEnd;
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
                    sc.on(importBlockOpen).shiftTo(IMPORT_BLOCK_BODY).exec(length ->
                            this.createCurrentToken(TokenType.IMPORT_BLOCK_OPEN, length)
                    );
                    sc.on(dollarScriptletOpen).shiftTo(DOLLAR_SCRIPTLET_BODY).exec(length ->
                            this.createCurrentToken(TokenType.DOLLAR_SCRIPTLET_OPEN, length)
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

                .whileIn(IMPORT_BLOCK_BODY, sc -> {
                    sc.on(importBlockBody).shiftTo(IMPORT_BLOCK_CLOSE).exec(length ->
                            this.createCurrentToken(TokenType.IMPORT_BLOCK_BODY, length)
                    );
                    sc.on(importBlockClose).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.IMPORT_BLOCK_CLOSE, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .whileIn(IMPORT_BLOCK_CLOSE, sc -> {
                    sc.on(importBlockClose).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.IMPORT_BLOCK_CLOSE, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .whileIn(DOLLAR_SCRIPTLET_BODY, sc -> {
                    sc.on(dollarScriptletBody).shiftTo(DOLLAR_SCRIPTLET_CLOSE).exec(length ->
                            this.createCurrentToken(TokenType.DOLLAR_SCRIPTLET_BODY, length)
                    );
                    sc.on(dollarScriptletClose).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.DOLLAR_SCRIPTLET_CLOSE, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .whileIn(DOLLAR_SCRIPTLET_CLOSE, sc -> {
                    sc.on(dollarScriptletClose).shiftTo(TEXT).exec(length ->
                            this.createCurrentToken(TokenType.DOLLAR_SCRIPTLET_CLOSE, length)
                    );
                    sc.onNoMatch().exec(this::done);
                })

                .build();
    }

    private void createCurrentToken(TokenType type, int length) {
        this.currentTokenType = type;
        this.currentTokenStart = this.currentIndex;
        this.currentTokenEnd = this.currentIndex + length;
    }

    private void done(CharSequence input) {
        this.done = true;
    }

    private void pullToken() {
        this.currentTokenState = this.fsm.getCurrentState();
        this.fsm.apply(
                this.currentInput.subSequence(
                        Math.max(this.currentIndex, this.inputStartIndex),
                        this.inputEndIndex
                )
        );
    }

    @Override
    public String toString() {
        return "FsmBasedTokenizer()";
    }

}
