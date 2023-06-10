package com.jessebrault.gst.util

import kotlin.test.assertFalse

fun assertNoDiagnostics(result: Result<*>) {
    assertFalse(result.hasDiagnostics(), "Result had diagnostics: ${ result.diagnostics }")
}