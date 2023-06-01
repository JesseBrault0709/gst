package com.jessebrault.gst.util;

import org.jetbrains.annotations.Nullable;

/**
 * May be extended to add additional properties/behavior.
 */
public class SimpleDiagnostic implements Diagnostic {

    private final String message;
    private final @Nullable Exception exception;

    public SimpleDiagnostic(String message) {
        this(message, null);
    }

    public SimpleDiagnostic(String message, @Nullable Exception exception) {
        this.message = message;
        this.exception = exception;
    }

    @Override
    public final String getMessage() {
        return this.message;
    }

    @Override
    public final @Nullable Exception getException() {
        return this.exception;
    }

    @Override
    public String toString() {
        return "SimpleDiagnostic(message: " + this.message + ", exception: " + this.exception + ")";
    }

}
