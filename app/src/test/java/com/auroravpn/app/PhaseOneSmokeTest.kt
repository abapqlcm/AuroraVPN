package com.auroravpn.app

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A unit test that actually runs. Phase 1 is about proving the toolchain works
 * end to end, so the assertion is deliberately simple.
 *
 * The debug build appends a version-name suffix, so the check matches on a
 * prefix rather than an exact string.
 */
class PhaseOneSmokeTest {
    @Test
    fun packageVersionIsSet() {
        assertTrue(
            "BuildConfig.VERSION_NAME was '${BuildConfig.VERSION_NAME}'",
            BuildConfig.VERSION_NAME.startsWith("2.0.0"),
        )
    }
}
