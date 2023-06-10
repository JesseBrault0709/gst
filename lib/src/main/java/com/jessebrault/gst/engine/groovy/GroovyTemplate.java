package com.jessebrault.gst.engine.groovy;

import com.jessebrault.gst.engine.Template;
import groovy.lang.Closure;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public final class GroovyTemplate implements Template {

    private final Closure<?> closure;

    public GroovyTemplate(Closure<?> closure) {
        this.closure = closure;
    }

    @Override
    public String make(Map<String, ?> binding) {
        final Closure<?> rehydrated = this.closure.rehydrate(binding, null, null);
        rehydrated.setResolveStrategy(Closure.DELEGATE_ONLY);
        final Writer w = new StringWriter();
        rehydrated.call(w);
        return w.toString();
    }

}
