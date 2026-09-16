plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt) apply false
    id("org.sonarqube") version "7.4.0.8496"
    id("org.cyclonedx.bom") version "3.3.0"
}

allprojects {
    group = "com.superplanner"
    version = "0.1.0"
}

subprojects {
    dependencyLocking {
        lockAllConfigurations()
    }

    // AGP/KSP create implementation-detail configurations dynamically. They are
    // intentionally excluded from locking because they do not have stable lock state.
    configurations.configureEach {
        if (name.startsWith("_") ||
            name.endsWith("DependenciesMetadata") ||
            name == "androidTestUtil" ||
            name == "androidJdkImage" ||
            name == "coreLibraryDesugaring" ||
            name == "debugWearBundling" ||
            name == "hiltCompileOnlyDebugAndroidTest" ||
            name == "hiltAnnotationProcessorDebugAndroidTest" ||
            name == "hiltAnnotationProcessorDebugUnitTest" ||
            name == "hiltAnnotationProcessorReleaseUnitTest" ||
            name.endsWith("AnnotationProcessorClasspath") ||
            name.contains("ksp") ||
            name.contains("androidTest") ||
            name.contains("UnitTest")
        ) {
            resolutionStrategy.deactivateDependencyLocking()
        }
    }

    pluginManager.withPlugin("dev.detekt") {
        extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
        }
    }
}

// Kept as a dedicated task because dependency-locks.yml invokes it with
// --write-locks. It resolves the same configurations used by CI without making
// strict locking a prerequisite for normal development builds.
tasks.register("resolveAndLockAll") {
    group = "dependency management"
    description = "Resolves CI dependencies and writes Gradle dependency locks."
    notCompatibleWithConfigurationCache("Generates dependency locks through normal task resolution")
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "Run this task with --write-locks"
        }
    }
    dependsOn(
        ":app:detekt",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":app:koverXmlReport",
        ":app:assembleDebug",
    )
}

sonar {
    properties {
        property("sonar.projectKey", "marciacrisrs_super-planner-app")
        property("sonar.organization", "marciacrisrs")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.kotlin.detekt.reportPaths", "$rootDir/app/build/reports/detekt/detekt.xml")
        property("sonar.androidLint.reportPaths", "$rootDir/app/build/reports/lint-results-debug.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "$rootDir/app/build/reports/kover/report.xml")
        property("sonar.junit.reportPaths", "$rootDir/app/build/test-results/testDebugUnitTest")
        property("sonar.coverage.exclusions", "**/BuildConfig.*,**/R.*,**/*Hilt_*,**/*_HiltModules*.*,**/*_Factory.*,**/*_MembersInjector*.*,**/ui/**,**/di/**")
        property("sonar.qualitygate.wait", "true")
    }
}

tasks.named("sonar") {
    dependsOn(":app:detekt", ":app:lintDebug", ":app:testDebugUnitTest", ":app:koverXmlReport")
}

tasks.register("verifyCi") {
    group = "verification"
    description = "Checks de CI antes do release (detekt, lint, testes e cobertura)"
    dependsOn(
        ":app:detekt",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":app:koverVerify",
    )
}
