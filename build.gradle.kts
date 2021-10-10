import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("org.optaplanner:optaplanner-core:8.11.1.Final")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "16"
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
