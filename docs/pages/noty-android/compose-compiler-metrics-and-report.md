# 📊 Compose compiler Metrics and Reports

NotyKT uses a Gradle Plugin **[PatilShreyas/compose-report-to-html](https://patilshreyas.github.io/compose-report-to-html/)** for generating Compose compiler metrics and report to understand the flaws in the Jetpack Compose's implementation.

▶️ [**BROWSE CURRENT REPORT**](https://patilshreyas.github.io/NotyKT/pages/noty-android/compose_report.html) ◀️

## How to use?

Refer to [***this pull request***](https://github.com/PatilShreyas/NotyKT/pull/607) to understand the change needed in the project and CI integration for generating and publishing compose compiler report.

- In the root project level `build.gradle`, add classpath for plugin:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "dev.shreyaspatil.compose-compiler-report-generator:gradle-plugin:$latestVersion"
  }
}
```

- Apply the plugin in which ***Jetpack compose is enabled***.

```groovy
apply plugin: "dev.shreyaspatil.compose-compiler-report-generator"

android {
    ...
}

htmlComposeCompilerReport {
    name = "NotyKT
}
```

> Follow the steps [mentioned here](https://patilshreyas.github.io/compose-report-to-html/use/using-gradle-plugin/) to use the plugin in the project.
