/*
 * Copyright (c) 2018-2026 NetFoundry Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openziti.android

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.openziti.ZitiContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore

/**
 * Needs a Ziti network, so it only runs when it is given an enrollment token:
 *
 * ```
 * ./gradlew :ziti-android:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.jwt=<enrollment token> \
 *     -Pandroid.testInstrumentationRunnerArguments.service=<intercept host of a service the identity can dial, optional> \
 *     -Pandroid.testInstrumentationRunnerArguments.seamless=<true to run Ziti in seamless mode, optional>
 * ```
 *
 * The service must answer an HTTP request on port 80.
 * Seamless mode replaces the socket implementation of the whole process, run it as the only test.
 */
@RunWith(AndroidJUnit4::class)
class EnrollmentIntegrationTest {

    private val args = InstrumentationRegistry.getArguments()
    private val jwt: String? = args.getString("jwt")
    private val service: String? = args.getString("service")
    private val seamless: Boolean = args.getString("seamless").toBoolean()

    private lateinit var app: Application
    private lateinit var keyStore: KeyStore

    @Before
    fun setup() {
        assumeNotNull(jwt)
        app = ApplicationProvider.getApplicationContext()
        keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        removeZitiEntries()
    }

    @After
    fun cleanup() {
        if (jwt == null) return
        org.openziti.Ziti.getContexts().toList().forEach { org.openziti.Ziti.removeContext(it) }
        removeZitiEntries()
    }

    @Test
    fun enrollRestartAndDelete() {
        Ziti.init(app, seamless)
        assertTrue(org.openziti.Ziti.getContexts().isEmpty())

        val name = Ziti.enroll(jwt!!)

        // the key is in the key store, where it can be used but not read
        val alias = keyStore.aliases().toList().single { keyStore.isKeyEntry(it) && it.startsWith("ziti://") }
        assertTrue("alias[$alias] has the identity name[$name]", alias.endsWith("/$name"))
        assertNull(keyStore.getKey(alias, null).encoded)
        assertTrue(keyStore.aliases().toList().any { it.startsWith("$alias-ca-") && keyStore.isCertificateEntry(it) })

        // first session: the identity runs from its configuration
        val ctx = org.openziti.Ziti.getContexts().single()
        awaitActive(ctx)

        // next start: the SDK only has the key store to load the identity from
        org.openziti.Ziti.removeContext(ctx)
        Ziti.init(app, seamless)
        val reloaded = org.openziti.Ziti.getContexts().single()
        assertFalse(ctx === reloaded)
        awaitActive(reloaded)

        service?.let {
            dialService(reloaded, it)
            if (seamless) {
                // an application that knows nothing about Ziti
                val http = URL("http://$it/").openConnection() as HttpURLConnection
                assertEquals(200, http.responseCode)
            }
        }

        Ziti.deleteIdentity(reloaded)
        assertTrue(org.openziti.Ziti.getContexts().isEmpty())
        assertTrue(
            "nothing left in the key store: ${keyStore.aliases().toList()}",
            keyStore.aliases().toList().none { it.startsWith("ziti:") }
        )
    }

    private fun awaitActive(ctx: ZitiContext, timeoutMs: Long = 30_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            when (val st = ctx.getStatus()) {
                is ZitiContext.Status.Active -> return
                is ZitiContext.Status.NotAuthorized -> fail("not authorized: ${st.ex}")
                else -> Thread.sleep(250)
            }
        }
        fail("identity is not active after ${timeoutMs}ms: ${ctx.getStatus()}")
    }

    private fun dialService(ctx: ZitiContext, host: String) {
        ctx.connect(host, 80).use { sock ->
            sock.soTimeout = 15_000
            sock.getOutputStream().apply {
                write("GET / HTTP/1.0\r\nHost: $host\r\n\r\n".toByteArray())
                flush()
            }
            val status = sock.getInputStream().bufferedReader().readLine()
            assertNotNull("response from $host", status)
            assertEquals("HTTP/1.0 200 OK", status)
        }
    }

    private fun removeZitiEntries() {
        keyStore.aliases().toList().filter { it.startsWith("ziti:") }.forEach { keyStore.deleteEntry(it) }
    }
}
