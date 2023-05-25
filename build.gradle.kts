import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("org.jetbrains.compose") version "1.4.0"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")

    implementation(compose.desktop.currentOs)

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "16"
        allWarningsAsErrors = true

        val composeReportsDir = buildDir.resolve("compose").absolutePath
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$composeReportsDir",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$composeReportsDir",
        )
    }
}

tasks.test {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR, TestLogEvent.FAILED)
    }

    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

detekt {
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}

tasks.check {
    // run detekt with type resolution
    dependsOn("detektMain")
    dependsOn("detektTest")
}

val resourcesDir: File = sourceSets["main"].resources.sourceDirectories.first()
val appProperties = resourcesDir.resolve("app.properties")
    .bufferedReader()
    .use { Properties().apply { load(it) } }
val version = appProperties["version"] as String

compose.desktop {
    application {
        mainClass = "com.tiquionophist.ui.MainKt"

        nativeDistributions {
            packageVersion = version
            targetFormats(TargetFormat.Msi)

            // remove logging and crypto libraries to slightly reduce application size
            modules = arrayListOf("java.base", "java.desktop")

            windows {
                iconFile.set(resourcesDir.resolve("app_icon.ico"))
            }
        }
    }
}

// convenience task which builds all relevant release artifacts into a single directory
tasks.create("createRelease") {
    mustRunAfter("clean")
    dependsOn("clean", "check", "package", "createDistributable", "packageUberJarForCurrentOS")

    doLast {
        val releaseDir = buildDir.resolve("release-$version")
        releaseDir.deleteRecursively()
        releaseDir.mkdirs()
        println("Created release destination directory $releaseDir")

        val composeDir = buildDir.resolve("compose")
        val msiSource = composeDir.resolve("binaries/main/msi/hhs-scheduler-$version.msi").absoluteFile
        val jarSource = composeDir.resolve("jars/hhs-scheduler-windows-x64-$version.jar").absoluteFile
        val distributableSourceDir = composeDir.resolve("binaries/main/app/hhs-scheduler").absoluteFile

        require(msiSource.isFile) { "msi source file does not exist" }
        require(jarSource.isFile) { "jar source file does not exist" }
        require(distributableSourceDir.isDirectory) { "distributable source directory does not exist" }

        val msiDestination = releaseDir.resolve("hhs-scheduler-$version.msi").absoluteFile
        val jarDestination = releaseDir.resolve("hhs-scheduler-$version.jar").absoluteFile
        val distributableDestination = releaseDir.resolve("hhs-scheduler-$version-standalone.zip").absoluteFile

        print("Copying MSI installer ${msiSource.toRelativeString(projectDir)} to ${msiDestination.toRelativeString(projectDir)}... ")
        msiSource.copyTo(target = msiDestination)
        println("success")

        print("Copying JAR ${jarSource.toRelativeString(projectDir)} to ${jarDestination.toRelativeString(projectDir)}... ")
        jarSource.copyTo(target = jarDestination)
        println("success")

        print("Creating zip file for standalone application at ${distributableSourceDir.toRelativeString(projectDir)} to ${distributableDestination.toRelativeString(projectDir)}... ")
        packZip(sourceDirectory = distributableSourceDir, destination = distributableDestination)
        println("success")

        println()
        println("Release artifacts successfully created at ${releaseDir.absolutePath}")
    }
}

/**
 * Creates a new zip file at [destination] containing the contents of [sourceDirectory].
 */
fun packZip(sourceDirectory: File, destination: File) {
    ZipOutputStream(destination.outputStream()).use { zos ->
        val sourcePath = sourceDirectory.toPath()
        Files.walk(sourcePath)
            .filter { !Files.isDirectory(it) }
            .forEach { path ->
                val zipEntry = ZipEntry(sourcePath.relativize(path).toString())
                zos.putNextEntry(zipEntry)
                Files.copy(path, zos)
                zos.closeEntry()
            }
    }
}
