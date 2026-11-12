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
import org.openziti.edge.model.PostureCheckType
import org.openziti.edge.model.PostureQuery
import org.openziti.edge.model.PostureResponseCreate
import org.openziti.edge.model.PostureResponseMacAddressCreate
import org.openziti.edge.model.PostureResponseOperatingSystemCreate
import org.openziti.posture.PostureService
import org.openziti.posture.PostureServiceProvider
import java.net.NetworkInterface

class PostureProvider: PostureServiceProvider {
    override fun getPostureService(): PostureService = AndroidPostureService

    object AndroidPostureService: PostureService {
        val androidVersion: String by lazy {
            Build.VERSION.RELEASE.split(".").plus(arrayOf("0","0")).take(3).joinToString(".")
        }
        val androidBuild: String by lazy {
            Build.VERSION.SECURITY_PATCH.replace("-", "") // turn date into an int
        }

        val macAddresses: Array<String> by lazy {
            NetworkInterface.getNetworkInterfaces().toList()
                .map { it.hardwareAddress }
                .filterNotNull()
                .map { it.joinToString(":"){ b -> "%02x".format(b) } }
                .toTypedArray()
        }

        val queries = mutableMapOf<String, PostureQuery>()

        override fun registerServiceCheck(serviceId: String, query: PostureQuery) {
            queries.put(query.id, query)
        }

        override fun getPosture(): List<PostureResponseCreate> = queries.values
            .mapNotNull { processQuery(it) }

        internal fun processQuery(q: PostureQuery): PostureResponseCreate? = when(q.queryType) {
            PostureCheckType.OS -> PostureResponseOperatingSystemCreate().apply {
                id(q.id)
                typeId(PostureCheckType.OS)
                type("android")
                version(androidVersion)
                build(androidBuild)
            }
            PostureCheckType.MAC -> PostureResponseMacAddressCreate().apply {
                id(q.id)
                typeId(PostureCheckType.MAC)
                macAddresses(macAddresses.toList())
            }
            else -> null
        }
    }
}