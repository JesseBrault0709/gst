package com.jessebrault.gst.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Result<T> {

    public static <T> Result<T> of(T t) {
        return new Result<>(t, List.of());
    }

    public static <T> Result<T> empty() {
        return new Result<>(null, List.of());
    }

    public static <T> Result<T> ofDiagnostics(Collection<Diagnostic> diagnostics) {
        return new Result<>(null, diagnostics);
    }

    private final T t;
    private final Collection<Diagnostic> diagnostics;

    private Result(T t, Collection<Diagnostic> diagnostics) {
        this.t = t;
        this.diagnostics = diagnostics;
    }

    public boolean hasDiagnostics() {
        return !this.diagnostics.isEmpty();
    }

    public T get() {
        return Objects.requireNonNull(this.t);
    }

    public Collection<Diagnostic> getDiagnostics() {
        return this.diagnostics;
    }

}
