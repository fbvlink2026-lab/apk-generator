# 📋 Talaan ng mga Build

> Created & Developed by MartoDosko © Copyright 2026

---
📅 **Petsa:** 2026-08-24 07:33 UTC
🎯 **Target:** GitHubUpdater
---

==================================================
📦 APLIKASYON: GitHubUpdater
📂 LOKASYON:   apps/GitHubUpdater
⏰ ORAS:       2026-08-24 07:33:03 UTC
==================================================

❌ NABIGO — Hindi nabuo o hindi mahanap ang APK
🔑 Exit Code: 1

--------------------------------------------------
📋 BUONG LOG NG BUILD:
--------------------------------------------------
Downloading https://services.gradle.org/distributions/gradle-8.2-bin.zip
............10%............20%............30%.............40%............50%............60%............70%.............80%............90%............100%
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:checkKotlinGradlePluginConfigurationErrors
> Task :app:generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug

> Task :app:parseDebugLocalResources FAILED
[Fatal Error] activity_main.xml:46:49: Element type "Button" must be followed by either attribute specifications, ">" or "/>".

> Task :app:mergeDebugResources FAILED
> Task :app:processDebugMainManifest

FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:parseDebugLocalResources'.
> A failure occurred while executing com.android.build.gradle.internal.res.ParseLibraryResourcesTask$ParseResourcesRunnable
   > Failed to parse XML file '/home/runner/work/apk-generator/apk-generator/apps/GitHubUpdater/app/build/intermediates/packaged_res/debug/layout/activity_main.xml'

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
==============================================================================

2: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:mergeDebugResources'.
> A failure occurred while executing com.android.build.gradle.internal.res.ResourceCompilerRunnable
   > Resource compilation failed (Failed to compile resource file: /home/runner/work/apk-generator/apk-generator/apps/GitHubUpdater/app/src/main/res/layout/activity_main.xml: . Cause: javax.xml.stream.XMLStreamException: ParseError at [row,col]:[46,49]
     Message: Element type "Button" must be followed by either attribute specifications, ">" or "/>".). Check logs for more details.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
==============================================================================

BUILD FAILED in 11s
10 actionable tasks: 10 executed

==================================================


---
✅ **Katapusan ng Talaan**
