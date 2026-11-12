/*
 * Copyright (c) 2018-2020 NetFoundry, Inc.
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

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.openziti.edge.model.PostureCheckType
import org.openziti.edge.model.PostureQuery
import org.openziti.edge.model.PostureResponseMacAddressCreate
import org.openziti.edge.model.PostureResponseOperatingSystemCreate
import org.openziti.posture.PostureService

@RunWith(AndroidJUnit4::class)
class PostureProviderTest {

    lateinit var postureService: PostureService
    @Before
    fun setup() {
        postureService = PostureService()
    }
    @Test
    fun osPostureTest() {
        postureService.registerServiceCheck("servid", PostureQuery().id("checkid").queryType(PostureCheckType.OS))

        val pr = postureService.getPosture()[0]
        assertEquals(PostureCheckType.OS, pr.typeId)
        val os = pr as PostureResponseOperatingSystemCreate
        assertEquals("android", os.type)
        val verComp = os.version.split(".")
        assertEquals(3, verComp.size)
    }

    @Test
    fun macPostureTest() {
        postureService.registerServiceCheck("servid", PostureQuery().id("checkid").queryType(PostureCheckType.MAC))

        val pr = postureService.getPosture()[0]
        assertEquals(PostureCheckType.MAC, pr.typeId)
        val mac = pr as PostureResponseMacAddressCreate
        // apps can not read hardware addresses since Android 11
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            assertTrue("MAC addresses: ${mac.macAddresses}", mac.macAddresses.isNotEmpty())
        }
    }

}