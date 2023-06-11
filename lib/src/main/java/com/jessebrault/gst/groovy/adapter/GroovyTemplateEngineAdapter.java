package com.jessebrault.gst.groovy.adapter;

import com.jessebrault.gst.groovy.GroovyTemplate;
import com.jessebrault.gst.groovy.GroovyTemplateCreator;
import com.jessebrault.gst.parser.Parser;
import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public final class GroovyTemplateEngineAdapter extends TemplateEngine {

    private static final class GroovyTemplateAdapter implements Template {

        private final GroovyTemplate delegate;

        public GroovyTemplateAdapter(GroovyTemplate delegate) {
            this.delegate = delegate;
        }

        @Override
        public Writable make() {
            return writer -> {
                writer.write(this.delegate.make(Map.of()));
                return writer;
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public Writable make(Map binding) {
            return writer -> {
                writer.write(this.delegate.make(binding));
                return writer;
            };
        }

    }

    private final Supplier<Parser> parserSupplier;
    private final Collection<URL> urls;
    private final ClassLoader parentClassLoader;
    private final Collection<String> customImportStatements;

    public GroovyTemplateEngineAdapter(
            Supplier<Parser> parserSupplier,
            Collection<URL> urls,
            ClassLoader parentClassLoader,
            Collection<String> customImportStatements
    ) {
        this.parserSupplier = parserSupplier;
        this.urls = urls;
        this.parentClassLoader = parentClassLoader;
        this.customImportStatements = customImportStatements;
    }

    @Override
    public Template createTemplate(Reader reader)
            throws CompilationFailedException, ClassNotFoundException, IOException {
        final var templateCreator = new GroovyTemplateCreator(
                this.parserSupplier,
                this.urls,
                this.parentClassLoader,
                false
        );
        final Writer w = new StringWriter();
        reader.transferTo(w);
        final var result = templateCreator.create(w.toString(), this.customImportStatements);
        if (result.hasDiagnostics()) {
            for (final var diagnostic : result.getDiagnostics()) {
                final var exception = diagnostic.getException();
                if (exception instanceof CompilationFailedException compilationFailedException) {
                    throw compilationFailedException;
                } else if (exception instanceof ClassNotFoundException classNotFoundException) {
                    throw classNotFoundException;
                } else if (exception instanceof IOException ioException) {
                    throw ioException;
                } else {
                    throw new RuntimeException(diagnostic.getMessage());
                }
            }
            throw new RuntimeException("Should not get here.");
        } else {
            return new GroovyTemplateAdapter((GroovyTemplate) result.get());
        }
    }

}
