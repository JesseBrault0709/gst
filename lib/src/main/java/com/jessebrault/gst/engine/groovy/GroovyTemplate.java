package com.jessebrault.gst.engine.groovy;

import com.jessebrault.gst.engine.Template;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public final class GroovyTemplate implements Template {

    private final GroovyObject scriptObject;
    private final Closure<?> closure;

    public GroovyTemplate(GroovyObject scriptObject, Closure<?> closure) {
        this.scriptObject = scriptObject;
        this.closure = closure;
    }

    @Override
    public String make(Map<String, ?> binding) {
        final Closure<?> rehydrated = this.closure.rehydrate(binding, this.scriptObject, this.scriptObject);
        rehydrated.setResolveStrategy(Closure.DELEGATE_ONLY);
        final Writer w = new StringWriter();
        rehydrated.call(w);
        return w.toString();
    }

}
