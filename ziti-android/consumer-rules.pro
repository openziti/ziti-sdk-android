#
# Copyright (c) 2018-2026 NetFoundry Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# The rules for the Ziti SDK itself (java.net.http, jjwt, the classes bound to JSON) come with the SDK.

# lazysodium-java is replaced by lazysodium-android (see build.gradle), so the SDK's default
# crypto loader, which uses it, is never loaded
-dontwarn com.goterl.lazysodium.LazySodiumJava
-dontwarn com.goterl.lazysodium.SodiumJava
-dontwarn com.goterl.lazysodium.utils.LibraryLoader$Mode

# JNA binds the libsodium functions by reflection
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.** { public *; }
-keep class com.goterl.lazysodium.** { *; }
-dontwarn java.awt.**
