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
import org.openziti.util.SystemInfo
import org.openziti.util.SystemInfoProvider

class AndroidSystemInfoProvider: SystemInfoProvider {
    override fun getSystemInfo(): SystemInfo = SystemInfo("Android",
        Build.VERSION.RELEASE,
        Build.VERSION.SECURITY_PATCH,
        System.getProperty("os.arch") ?: ""
        )
}