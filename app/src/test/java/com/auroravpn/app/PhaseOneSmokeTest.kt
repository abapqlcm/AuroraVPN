package com.auroravpn.app

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A unit test that actually runs. Phase 1 is about proving the toolchain works
 * end to end, so the assertion is trivial on purpose.
 */
class PhaseOneSmokeTest {
    @Test
    fun packageVersionIsSet() {
        assertEquals("2.0.0", BuildConfig.VERSION_NAME)
    }
}
