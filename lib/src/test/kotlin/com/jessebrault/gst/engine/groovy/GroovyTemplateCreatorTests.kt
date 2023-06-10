package com.jessebrault.gst.engine.groovy

import com.jessebrault.gst.parser.StandardGstParser

class GroovyTemplateCreatorTests : AbstractGroovyTemplateCreatorTests(
        GroovyTemplateCreator(
                ::StandardGstParser,
                emptyList(),
                Thread.currentThread().contextClassLoader,
                true
        )
)