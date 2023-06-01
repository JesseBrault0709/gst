package com.jessebrault.gst.util;

import org.jetbrains.annotations.Nullable;

public interface Diagnostic {
    String getMessage();
    @Nullable Exception getException();
}
