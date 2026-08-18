# Talaan ng mga Error

> Created & Developed by MartoDosko © Copyright 2026

✅ TAGUMPAY — GuitarFX — 2026-08-18 16:08 UTC — Laki: 3.1M — APK NA SA ARTIFACTS!

### 📋 BUONG LOG:
```
> Task :app:checkKotlinGradlePluginConfigurationErrors
> Task :app:generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:parseDebugLocalResources

> Task :app:processDebugMainManifest
package="com.martodosko.guitarfx" found in source AndroidManifest.xml: /home/runner/work/apk-generator/apk-generator/apps/GuitarFX/app/src/main/AndroidManifest.xml.
Setting the namespace via the package attribute in the source AndroidManifest.xml is no longer supported, and the value is ignored.
Recommendation: remove package="com.martodosko.guitarfx" from the source AndroidManifest.xml: /home/runner/work/apk-generator/apk-generator/apps/GuitarFX/app/src/main/AndroidManifest.xml.

> Task :app:processDebugManifest
> Task :app:javaPreCompileDebug
> Task :app:mergeDebugShaders
> Task :app:compileDebugShaders NO-SOURCE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:compressDebugAssets
> Task :app:desugarDebugFileDependencies
> Task :app:processDebugManifestForPackage
> Task :app:checkDebugDuplicateClasses
> Task :app:mergeDebugJniLibFolders
> Task :app:mergeLibDexDebug
> Task :app:mergeDebugNativeLibs NO-SOURCE
> Task :app:stripDebugDebugSymbols NO-SOURCE
> Task :app:processDebugResources
> Task :app:validateSigningDebug
> Task :app:writeDebugAppMetadata
> Task :app:writeDebugSigningConfigVersions
> Task :app:mergeExtDexDebug
> Task :app:compileDebugKotlin
> Task :app:compileDebugJavaWithJavac NO-SOURCE
> Task :app:dexBuilderDebug
> Task :app:mergeDebugGlobalSynthetics
> Task :app:processDebugJavaRes
> Task :app:mergeProjectDexDebug
> Task :app:mergeDebugJavaResource
> Task :app:packageDebug
> Task :app:createDebugApkListingFileRedirect
> Task :app:assembleDebug

BUILD SUCCESSFUL in 26s
33 actionable tasks: 33 executed
EXIT_CODE=0
=== END BUILD: Tue Aug 18 16:08:38 UTC 2026 ===
```
---
❌ NABIGO — MartoDosko Updater — 2026-08-18 16:00 UTC — WALANG APK! Code: 

### ⚠️ WALANG LOG FILE SA: 
---
### 📋 BUONG LOG:
```
License for package Android SDK Build-Tools 33.0.1 accepted.
Preparing "Install Android SDK Build-Tools 33.0.1 v.33.0.1".
"Install Android SDK Build-Tools 33.0.1 v.33.0.1" ready.
Installing Android SDK Build-Tools 33.0.1 in /usr/local/lib/android/sdk/build-tools/33.0.1
"Install Android SDK Build-Tools 33.0.1 v.33.0.1" complete.
"Install Android SDK Build-Tools 33.0.1 v.33.0.1" finished.
> Task :preBuild UP-TO-DATE
> Task :preDebugBuild UP-TO-DATE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :generateDebugResValues
> Task :checkDebugAarMetadata
> Task :mapDebugSourceSetPaths
> Task :generateDebugResources
> Task :packageDebugResources
> Task :mergeDebugResources
> Task :createDebugCompatibleScreenManifests
> Task :extractDeepLinksDebug
> Task :parseDebugLocalResources
> Task :processDebugMainManifest
> Task :processDebugManifest
> Task :javaPreCompileDebug
> Task :mergeDebugShaders
> Task :compileDebugShaders NO-SOURCE
> Task :generateDebugAssets UP-TO-DATE
> Task :mergeDebugAssets FROM-CACHE
> Task :compressDebugAssets
> Task :desugarDebugFileDependencies
> Task :processDebugManifestForPackage
> Task :checkDebugDuplicateClasses
> Task :processDebugResources FAILED
> Task :mergeExtDexDebug

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':processDebugResources'.
> A failure occurred while executing com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask$TaskAction
   > Android resource linking failed
     /home/runner/work/apk-generator/apk-generator/apps/GuitarFX/build/intermediates/packaged_manifests/debug/AndroidManifest.xml:17: error: resource mipmap/ic_launcher (aka com.guitarfx.app:mipmap/ic_launcher) not found.
     error: failed processing manifest.


* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 19s
19 actionable tasks: 18 executed, 1 from cache
```

---
### 📋 BUONG LOG:
```

> Task :app:preBuild UP-TO-DATE
> Task :preBuild UP-TO-DATE
> Task :preDebugBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:generateDebugResValues
> Task :generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :mapDebugSourceSetPaths
> Task :generateDebugResources
> Task :app:generateDebugResources
> Task :packageDebugResources
> Task :app:packageDebugResources
> Task :parseDebugLocalResources
> Task :createDebugCompatibleScreenManifests
> Task :extractDeepLinksDebug
> Task :app:parseDebugLocalResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :mergeDebugResources
> Task :processDebugMainManifest FAILED
> Task :app:mergeDebugResources
> Task :app:processDebugMainManifest

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':processDebugMainManifest' (type 'ProcessApplicationManifest').
  - In plugin 'com.android.internal.version-check' type 'com.android.build.gradle.tasks.ProcessApplicationManifest' property 'mainManifest' specifies file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/src/main/AndroidManifest.xml' which doesn't exist.
    
    Reason: An input file was expected to be present but it doesn't exist.
    
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.
    
    For more information, please refer to https://docs.gradle.org/8.2/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 7s
18 actionable tasks: 17 executed, 1 from cache
```

---
### 📋 BUONG LOG:
```

> Task :app:preBuild UP-TO-DATE
> Task :preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :preDebugBuild UP-TO-DATE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :generateDebugResValues
> Task :app:generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :checkDebugAarMetadata
> Task :mapDebugSourceSetPaths
> Task :generateDebugResources
> Task :app:mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :packageDebugResources
> Task :app:packageDebugResources
> Task :parseDebugLocalResources
> Task :createDebugCompatibleScreenManifests
> Task :extractDeepLinksDebug
> Task :app:parseDebugLocalResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :mergeDebugResources
> Task :processDebugMainManifest FAILED
> Task :app:mergeDebugResources
> Task :app:processDebugMainManifest

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':processDebugMainManifest' (type 'ProcessApplicationManifest').
  - In plugin 'com.android.internal.version-check' type 'com.android.build.gradle.tasks.ProcessApplicationManifest' property 'mainManifest' specifies file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/src/main/AndroidManifest.xml' which doesn't exist.
    
    Reason: An input file was expected to be present but it doesn't exist.
    
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.
    
    For more information, please refer to https://docs.gradle.org/8.2/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 11s
18 actionable tasks: 17 executed, 1 from cache
```

---
### 📋 BUONG LOG:
```

> Task :app:preBuild UP-TO-DATE
> Task :preBuild UP-TO-DATE
> Task :preDebugBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:generateDebugResValues
> Task :generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :generateDebugResources
> Task :packageDebugResources
> Task :app:packageDebugResources
> Task :mergeDebugResources
> Task :app:parseDebugLocalResources
> Task :createDebugCompatibleScreenManifests
> Task :app:createDebugCompatibleScreenManifests
> Task :extractDeepLinksDebug
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :app:mergeDebugResources
> Task :parseDebugLocalResources
> Task :app:processDebugMainManifest FAILED
> Task :processDebugMainManifest

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':app:processDebugMainManifest' (type 'ProcessApplicationManifest').
  - In plugin 'com.android.internal.version-check' type 'com.android.build.gradle.tasks.ProcessApplicationManifest' property 'mainManifest' specifies file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/app/src/main/AndroidManifest.xml' which doesn't exist.
    
    Reason: An input file was expected to be present but it doesn't exist.
    
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.
    
    For more information, please refer to https://docs.gradle.org/8.2/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 56s
18 actionable tasks: 17 executed, 1 from cache
```

---
### 📋 BUONG LOG:
```

> Task :app:preBuild UP-TO-DATE
> Task :preBuild UP-TO-DATE
> Task :preDebugBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :generateDebugResValues
> Task :app:generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :mapDebugSourceSetPaths
> Task :generateDebugResources
> Task :app:generateDebugResources
> Task :packageDebugResources
> Task :app:packageDebugResources
> Task :mergeDebugResources
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :extractDeepLinksDebug
> Task :parseDebugLocalResources
> Task :app:parseDebugLocalResources
> Task :processDebugMainManifest FAILED
> Task :app:processDebugMainManifest

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':processDebugMainManifest' (type 'ProcessApplicationManifest').
  - In plugin 'com.android.internal.version-check' type 'com.android.build.gradle.tasks.ProcessApplicationManifest' property 'mainManifest' specifies file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/src/main/AndroidManifest.xml' which doesn't exist.
    
    Reason: An input file was expected to be present but it doesn't exist.
    
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.
    
    For more information, please refer to https://docs.gradle.org/8.2/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 7s
18 actionable tasks: 18 executed
```

---
### 📋 BUONG LOG:
```
> Task :app:preDebugBuild UP-TO-DATE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:generateDebugResValues
> Task :generateDebugResValues
> Task :checkDebugAarMetadata
> Task :app:checkDebugAarMetadata
> Task :mapDebugSourceSetPaths
> Task :app:mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :generateDebugResources
> Task :packageDebugResources
> Task :app:packageDebugResources
> Task :mergeDebugResources
> Task :app:mergeDebugResources
> Task :createDebugCompatibleScreenManifests
> Task :app:createDebugCompatibleScreenManifests
> Task :extractDeepLinksDebug
> Task :app:extractDeepLinksDebug
> Task :parseDebugLocalResources
> Task :app:parseDebugLocalResources
> Task :processDebugMainManifest FAILED

> Task :app:processDebugMainManifest
package="com.martodosko.guitarfx" found in source AndroidManifest.xml: /home/runner/work/apk-generator/apk-generator/apps/GuitarFX/app/src/main/AndroidManifest.xml.
Setting the namespace via the package attribute in the source AndroidManifest.xml is no longer supported, and the value is ignored.
Recommendation: remove package="com.martodosko.guitarfx" from the source AndroidManifest.xml: /home/runner/work/apk-generator/apk-generator/apps/GuitarFX/app/src/main/AndroidManifest.xml.

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':processDebugMainManifest' (type 'ProcessApplicationManifest').
  - In plugin 'com.android.internal.version-check' type 'com.android.build.gradle.tasks.ProcessApplicationManifest' property 'mainManifest' specifies file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/src/main/AndroidManifest.xml' which doesn't exist.
    
    Reason: An input file was expected to be present but it doesn't exist.
    
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.
    
    For more information, please refer to https://docs.gradle.org/8.2/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 6s
18 actionable tasks: 18 executed
```

---
### 📋 BUONG LOG:
```
Downloading https://services.gradle.org/distributions/gradle-8.2-bin.zip
............10%............20%............30%.............40%............50%............60%............70%.............80%............90%............100%

Welcome to Gradle 8.2!

Here are the highlights of this release:
 - Kotlin DSL: new reference documentation, assignment syntax by default
 - Kotlin DSL is now the default with Gradle init
 - Improved suggestions to resolve errors in console output

For more details see https://docs.gradle.org/8.2/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)

FAILURE: Build failed with an exception.

* Where:
Build file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/build.gradle' line: 13

* What went wrong:
A problem occurred evaluating root project 'GuitarFX'.
> Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'build.gradle'

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 33s
```

---
### 📋 BUONG LOG:
```
Downloading https://services.gradle.org/distributions/gradle-8.2-bin.zip
............10%............20%............30%.............40%............50%............60%............70%.............80%............90%............100%

Welcome to Gradle 8.2!

Here are the highlights of this release:
 - Kotlin DSL: new reference documentation, assignment syntax by default
 - Kotlin DSL is now the default with Gradle init
 - Improved suggestions to resolve errors in console output

For more details see https://docs.gradle.org/8.2/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)

FAILURE: Build failed with an exception.

* Where:
Settings file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/settings.gradle' line: 1

* What went wrong:
Could not compile settings file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/settings.gradle'.
> startup failed:
  settings file '/home/runner/work/apk-generator/apk-generator/apps/GuitarFX/settings.gradle': 1: Unexpected input: '{' @ line 1, column 18.
     pluginManagement {
                      ^
  
  1 error


* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 7s
```

---
### 📋 BUONG LOG:
```
Error: Could not find or load main class "-Xmx64m"
Caused by: java.lang.ClassNotFoundException: "-Xmx64m"
```

---
### 📋 BUONG LOG:
```
Error: Could not find or load main class "-Xmx64m"
Caused by: java.lang.ClassNotFoundException: "-Xmx64m"
```

---
❌ ERROR — GuitarFX — 2026-08-17 11:25 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 11:11 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 11:09 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 10:59 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 10:56 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 10:52 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 10:43 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 10:36 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 10:31 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-17 08:29 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 10:20 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 10:06 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 09:56 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 09:46 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 09:39 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 09:32 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 09:19 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 09:11 UTC — Nabigo ang pagbuo
---
❌ ERROR — GuitarFX — 2026-08-16 08:01 UTC — Nabigo ang pagbuo
---
---
