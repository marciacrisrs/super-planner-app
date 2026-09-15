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

subprojects {
    dependencyLocking {
        lockAllConfigurations()
        lockMode = org.gradle.api.artifacts.dsl.LockMode.STRICT
    }

    // Gradle/AGP/KSP create implementation-detail configurations that must not
    // participate in dependency locking. These configurations are created
    // dynamically by the Android/KSP toolchain and do not have stable lock state.
    configurations.matching {
        it.name.startsWith("_") ||
            it.name.endsWith("DependenciesMetadata") ||
            it.name == "androidTestUtil" ||
            it.name == "androidJdkImage" ||
            it.name == "coreLibraryDesugaring" ||
            it.name == "debugWearBundling" ||
            it.name.endsWith("AnnotationProcessorClasspath")
    }.configureEach {
        resolutionStrategy.deactivateDependencyLocking()
    }

    pluginManager.withPlugin("dev.detekt") {
        extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
        }
    }
}

tasks.register("resolveAndLockAll") {
    group = "dependency management"
    description = "Generates dependency locks through Gradle's normal task resolution."
    notCompatibleWithConfigurationCache("Generates dependency locks through normal task resolution")
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "Run this task with --write-locks"
        }
    }
    // Generate lock state from normal build/verification resolution only.
    // koverVerify is intentionally excluded: it is a quality gate and must not
    // block dependency-lock generation when coverage is below the CI threshold.
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
