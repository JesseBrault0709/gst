package com.jessebrault.gst.engine;

import java.util.Map;

public interface Template {

    String make(Map<String, ?> binding);

    default String make() {
        return this.make(Map.of());
    }

}
