
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.30"
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
    id("org.jetbrains.compose") version "1.0.0-alpha4-build366"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.18.1")

    implementation(compose.desktop.currentOs)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.optaplanner:optaplanner-core:8.11.1.Final")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.time.ExperimentalTime",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
        )
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

detekt {
    toolVersion = "1.18.1"
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}

val resourcesDir = sourceSets["main"].resources.sourceDirectories.first()
val appProperties = resourcesDir.resolve("app.properties")
    .bufferedReader()
    .use { Properties().apply { load(it) } }

compose.desktop {
    application {
        mainClass = "com.tiquionophist.ui.MainKt"

        nativeDistributions {
            packageVersion = appProperties.getProperty("version")
            targetFormats(TargetFormat.Exe)

            windows {
                iconFile.set(resourcesDir.resolve("app_icon.ico"))
            }
        }
    }
}
