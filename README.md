[![Maven Central](https://img.shields.io/maven-central/v/io.github.mflisar.kmpicon/plugin?style=for-the-badge&color=blue)](https://central.sonatype.com/artifact/io.github.mflisar.kmpicon/plugin) ![API](https://img.shields.io/badge/api-23%2B-brightgreen.svg?style=for-the-badge) ![Kotlin](https://img.shields.io/github/languages/top/MFlisar/KMPIcon.svg?style=for-the-badge&amp;color=blueviolet) ![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-blue?style=for-the-badge&amp;label=Kotlin)
# KMPIcon
![Platforms](https://img.shields.io/badge/PLATFORMS-black?style=for-the-badge)

This library allows you to generator icons for android, iOS, windows and general compose multiplatform from a single source of truth.

It supports following formats:
* SVG
* PNG

# Table of Contents

- [Supported Platforms](#computer-supported-platforms)
- [Versions](#arrow_right-versions)
- [Setup](#wrench-setup)
- [Usage](#rocket-usage)
- [Demo](#sparkles-demo)
- [API](#books-api)
- [Other Libraries](#bulb-other-libraries)

# :computer: Supported Platforms

| Module |
|---|
| plugin |

# :arrow_right: Versions

| Dependency | Version |
|---|---|
| Kotlin | `2.3.20` |
| Jetbrains Compose | `1.9.3` |
| Jetbrains Compose Material3 | `1.9.0` |

# :wrench: Setup

<details open>

<summary><b>Using Version Catalogs</b></summary>

<br>

Define the dependencies inside your **libs.versions.toml** file.

```toml
[versions]

kmpicon = "<LATEST-VERSION>"

[libraries]

kmpicon-plugin = { module = "io.github.mflisar.kmpicon:plugin", version.ref = "kmpicon" }
```

And then use the definitions in your projects **build.gradle.kts** file like following:

```java
implementation(libs.kmpicon.plugin)
```

</details>

<details>

<summary><b>Direct Dependency Notation</b></summary>

<br>

Simply add the dependencies inside your **build.gradle.kts** file.

```kotlin
val kmpicon = "<LATEST-VERSION>"

implementation("io.github.mflisar.kmpicon:plugin:${kmpicon}")
```

</details>

# :rocket: Usage

This library is used like following:

* generate a icon with `AndroidStudio`
* use this plugin to copy and/or create images for different use cases

**Create a common main icon**

```kotlin
kmpIcon {

    setup {
        sourceModule = "demo/app/android" // path of the android module where the source file is located (default: app/app/android")
        sourceFile = "src/main/ic_launcher-playstore.png" // relative path to the source file (default: src/main/ic_launcher.png)
    }

    generateCommonIcon {
        file = "src/commonMain/composeResources/drawable/icon.png" // relative path to the output file (default: src/commonMain/composeResources/drawable/icon.png)
    }
}
```

**Create a ico for windows**

```kotlin
kmpIcon {

    setup {
        sourceModule = "demo/app/android" // path of the android module where the source file is located (default: app/app/android")
        sourceFile = "src/main/ic_launcher-playstore.png" // relative path to the source file (default: src/main/ic_launcher.png)
    }

    generateIco {
        file = "icon.ico" // relative path to the output file (default: icon.ico)
    }
}
```

# :sparkles: Demo

A full [demo](/demo) is included inside the demo module, it shows nearly every usage with working examples.

# :books: API

Check out the [API documentation](https://MFlisar.github.io/KMPIcon/).

# :bulb: Other Libraries

You can find more libraries (all multiplatform) of mine that all do work together nicely [here](https://mflisar.github.io/Libraries/).
