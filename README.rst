.. image:: https://raw.githubusercontent.com/openziti/branding/main/images/banners/Android.jpg
    :alt: Ziggy using the ziti-sdk-android

.. image:: https://github.com/openziti/ziti-sdk-android/workflows/CI%20build/badge.svg
    :target: https://github.com/openziti/ziti-sdk-android/actions?query=workflow%3A%22CI+build%22

.. image:: https://img.shields.io/maven-central/v/org.openziti/ziti-android
    :alt: Maven Central version
    :target: https://central.sonatype.com/artifact/org.openziti/ziti-android

Ziti SDK for Android
====================
The **Ziti SDK for Android** enables developers to easily and securely connect their
Android applications
to backend services over `OpenZiti <https://github.com/openziti/ziti>`_ networks.

.. contents::


Release Notes
-------------
Release notes are published with each `release <https://github.com/openziti/ziti-sdk-android/releases>`_ on GitHub.


Obtaining SDK
-------------
The recommended way to use the Ziti SDK for Android in your project is to add the dependency
using your favorite build tool.
Our artifacts are hosted on Maven Central, so add the repo and dependency to your project.
The latest version is shown in the Maven Central badge at the top of this page, use it in place of ``VERSION`` below.

Maven
_____
.. code-block:: xml

  <project>
     ....
     <repositories>
        ....
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo.maven.apache.org/maven2/</url>
         </repository>
     </repositories>
     ....
     <dependencies>
        ...
        <dependency>
           <groupId>org.openziti</groupId>
           <artifactId>ziti-android</artifactId>
           <version>VERSION</version>
        </dependency>
     </dependencies>
     ....
  </project>

Gradle
______
.. code-block:: gradle

   repositories {
       ...
       mavenCentral()
   }
   ...
   dependencies {
      ...
      implementation 'org.openziti:ziti-android:VERSION'
   }

Building from Source
--------------------
Once you check out the project from GitHub, you can build it using gradle,
with JDK 17 and the Android SDK installed (``ANDROID_HOME``, or ``sdk.dir`` in ``local.properties``):

.. code-block::

    ./gradlew build

    # install into your local maven cache
    ./gradlew :ziti-android:publishToMavenLocal


Features
--------
- allows to develop applications that connect to services on NetFoundry Ziti Service mesh
- strong identity support
- transparent use -- in seamless mode connections to network resources are intercepted and 
  routed over Ziti network for any Ziti service that identity has access to
- written in `Kotlin <https://kotlinlang.org/>`_ with Java-friendly API
- read more in our docs_

Enrollment
__________
See background `here <https://netfoundry.io/docs/openziti/learn/core-concepts/identities/enrolling>`_

An application enrolls with the *enrollment token* (a JWT file) of its identity.
``Ziti.enrollZiti()`` takes the token as a ``Uri`` or as bytes. ``EnrollmentActivity``, a file picker for the token,
is the default enrollment screen: ``Ziti.getEnrollmentIntent()`` returns the intent for it, and
``Ziti.setEnrollmentActivity()`` replaces it with the application's own.

Enrolling runs in the background and loads the new identity right away.
It creates two (or more) entries in the Android KeyStore:

private key entry
   client private key/certificate identifying the application instance. This entry is stored with
   ``ziti://<controller_host>:<port>/<identity id>`` alias, the key can be used to sign but not read out

CA certificate entries
   root CA certificates used to validate Ziti network components,
   stored as ``<private key entry alias>-ca-<certificate serial number>``

Note:

   This allows enrolling and storing multiple identities in the key store.
   However, typically an application instance would only have a single enrollment or identity

Android Support
---------------
Easiest way to start developing on Android with Ziti is to create your repo from
our `template project <https://github.com/openziti/ziti-android-app#readme>`_
or just look at the code for inspiration.

Identities are kept in the Android KeyStore (see Enrollment_), and ``Ziti.init()`` loads them from it.

Setting up an application
_________________________
- The Ziti SDK is built with Kotlin 2.4, the Kotlin compiler of the application has to be 2.3 or newer.
  This library is built with Android Gradle plugin 9, Gradle 9 and ``compileSdk`` 37.
- Kotlin applications add ``org.jetbrains.kotlinx:kotlinx-coroutines-android`` themselves,
  ``org.openziti.android.Ziti`` is a ``CoroutineScope``.
- Several of the SDK's dependencies carry the same license and manifest files, the application has to exclude them:

.. code-block:: gradle

   android {
       packaging {
           resources {
               excludes += ['META-INF/LICENSE*', 'META-INF/NOTICE*', 'META-INF/DEPENDENCIES',
                            'META-INF/versions/9/OSGI-INF/MANIFEST.MF']
           }
       }
   }

- Minified applications need no rules of their own: the rules for the SDK and for the libsodium and JNA libraries
  it uses come with the libraries.



Getting Help
------------
Please use these community resources for getting help. We use GitHub issues_
for tracking bugs and feature requests and have limited bandwidth
to address them.

- Read the docs_
- Join our `Developer Community`_
- Participate in discussion on Discourse_


.. _Developer Community: https://ziti.dev
.. _docs: https://netfoundry.io/docs/openziti/intro
.. _Discourse: https://openziti.discourse.group/
.. _issues: https://github.com/openziti/ziti-sdk-android/issues
