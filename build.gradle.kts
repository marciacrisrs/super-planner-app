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
    dependencyLocking {
        lockAllConfigurations()
        lockMode = org.gradle.api.artifacts.dsl.LockMode.STRICT
    }
}

subprojects {
    pluginManager.withPlugin("io.gitlab.arturbosch.detekt") {
        extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
        }
    }
}

tasks.register("resolveAndLockAll") {
    group = "dependency management"
    description = "Resolves all lockable configurations and writes Gradle dependency lockfiles."
    notCompatibleWithConfigurationCache("Resolves configurations dynamically to persist dependency locks")
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "Run this task with --write-locks"
        }
    }
    doLast {
        allprojects.forEach { project ->
            project.configurations
                .filter { it.isCanBeResolved }
                .forEach { it.resolve() }
        }
    }
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
