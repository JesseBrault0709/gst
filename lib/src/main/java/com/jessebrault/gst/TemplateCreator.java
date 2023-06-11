package com.jessebrault.gst;

import com.jessebrault.gst.util.Result;

import java.util.Collection;
import java.util.List;

public interface TemplateCreator {

    Result<Template> create(CharSequence input, Collection<String> customImportStatements);

    default Result<Template> create(CharSequence input) {
        return this.create(input, List.of());
    }

}
