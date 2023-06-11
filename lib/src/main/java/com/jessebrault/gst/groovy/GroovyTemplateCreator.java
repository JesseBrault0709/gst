package com.jessebrault.gst.groovy;

import com.jessebrault.gst.ast.AstPrettyPrinterVisitor;
import com.jessebrault.gst.ast.AstUtil;
import com.jessebrault.gst.ast.TreeNode;
import com.jessebrault.gst.ast.TreeNodeType;
import com.jessebrault.gst.Template;
import com.jessebrault.gst.TemplateCreator;
import com.jessebrault.gst.parser.Parser;
import com.jessebrault.gst.parser.TreeNodeParserAccumulator;
import com.jessebrault.gst.tokenizer.FsmBasedTokenizer;
import com.jessebrault.gst.tokenizer.TokenProvider;
import com.jessebrault.gst.tokenizer.TokenizerBasedTokenProvider;
import com.jessebrault.gst.tokenizer.TokenizerState;
import com.jessebrault.gst.util.Diagnostic;
import com.jessebrault.gst.util.Result;
import com.jessebrault.gst.util.SimpleDiagnostic;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * May be extended.
 */
public class GroovyTemplateCreator implements TemplateCreator {

    private static final Logger logger = LoggerFactory.getLogger(GroovyTemplateCreator.class);

    private final Supplier<Parser> parserSupplier;
    private final File tmpDir;
    private final GroovyScriptEngine engine;
    private final boolean debug;

    private int scriptNumber;

    public GroovyTemplateCreator(
            Supplier<Parser> parserSupplier,
            Collection<URL> urls,
            ClassLoader parentClassLoader,
            boolean debug
    ) throws IOException {
        this.parserSupplier = parserSupplier;
        this.debug = debug;
        final var tmpDirPath = Files.createTempDirectory("groovyTemplateCreator");
        this.tmpDir = tmpDirPath.toFile();
        final Collection<URL> allUrls = new ArrayList<>(urls);
        allUrls.add(tmpDirPath.toUri().toURL());
        this.engine = new GroovyScriptEngine(allUrls.toArray(URL[]::new), parentClassLoader);
    }

    protected TreeNode tokenizeAndParse(CharSequence input) {
        final TokenProvider tokenProvider = new TokenizerBasedTokenProvider(
                FsmBasedTokenizer::new,
                input,
                0,
                input.length(),
                TokenizerState.TEXT
        );
        final TreeNodeParserAccumulator acc = new TreeNodeParserAccumulator();
        final Parser parser = this.parserSupplier.get();
        parser.parse(tokenProvider, acc);
        final TreeNode root = acc.getResult();
        if (this.debug) {
            final var prettyPrinter = new AstPrettyPrinterVisitor();
            prettyPrinter.visitGString(root);
            logger.debug("Ast:\n{}\n", prettyPrinter.getResult());
        }
        return acc.getResult();
    }

    protected String transformToScript(
            CharSequence input,
            Collection<String> customImportStatements,
            TreeNode root
    ) {
        final var transformer = new GroovyAstToScriptTransformer(customImportStatements, input);
        transformer.visitGString(root);
        final String scriptText = transformer.getResult();
        if (this.debug) {
            logger.debug("Script:\n{}\n", scriptText);
        }
        return scriptText;
    }

    protected Result<Template> createTemplate(String scriptText) {
        final var scriptName = "groovyTemplateScript" + this.scriptNumber + ".groovy";
        final var scriptFile = new File(this.tmpDir, scriptName);
        this.scriptNumber++;
        try (final Writer scriptFileWriter = new FileWriter(scriptFile)) {
            scriptFileWriter.write(scriptText);
            scriptFileWriter.close();
            final Class<?> scriptClass = this.engine.loadScriptByName(scriptName);
            final var scriptObject = (GroovyObject) scriptClass.getDeclaredConstructor().newInstance();
            final var closure = (Closure<?>) scriptObject.invokeMethod("getTemplateClosure", null);
            return Result.of(new GroovyTemplate(scriptObject, closure));
        } catch (Exception e) {
            final Diagnostic diagnostic = new SimpleDiagnostic(
                    "An exception occurred while creating the template: " + e.getMessage(),
                    e
            );
            return Result.ofDiagnostics(List.of(diagnostic));
        }
    }

    @Override
    public Result<Template> create(CharSequence input, Collection<String> customImportStatements) {
        final TreeNode root = this.tokenizeAndParse(input);
        final Collection<Diagnostic> rootDiagnostics = AstUtil.getAllDiagnostics(root);
        if (rootDiagnostics.isEmpty()) {
            if (root.getType() == TreeNodeType.G_STRING) {
                final var scriptText = transformToScript(input, customImportStatements, root);
                return this.createTemplate(scriptText);
            } else {
                final Diagnostic diagnostic = new SimpleDiagnostic("input did not parse to a G_STRING");
                return Result.ofDiagnostics(List.of(diagnostic));
            }
        } else {
            return Result.ofDiagnostics(rootDiagnostics);
        }
    }

}
