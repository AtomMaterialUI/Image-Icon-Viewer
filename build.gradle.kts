/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2015-2022 Elior "Mallowigi" Boukhobza, David Sommer and Jonathan Lermitage.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@file:Suppress("SpellCheckingInspection", "HardCodedStringLiteral")

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

fun environment(key: String) = providers.environmentVariable(key)

fun fileProperties(key: String) = project.findProperty(key).toString().let { if (it.isNotEmpty()) file(it) else null }


plugins {
  signing
  // Java support
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.gradleIntelliJPlugin)
  alias(libs.plugins.changelog)
  alias(libs.plugins.qodana)
  alias(libs.plugins.detekt)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.kover)
}

group = properties("pluginGroup")
version = properties("pluginVersion")
val depsTwelveMonkeys = properties("depsTwelveMonkeys")

// Configure project's dependencies
repositories {
  mavenCentral()
  maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
  maven(url = "https://maven-central.storage-download.googleapis.com/repos/central/data/")
  maven(url = "https://www.jetbrains.com/intellij-repository/releases")
  maven(url = "https://www.jetbrains.com/intellij-repository/snapshots")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
  implementation("com.twelvemonkeys.imageio:imageio-core:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-metadata:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-sgi:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-psd:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-tiff:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-pdf:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-icns:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-pcx:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-pnm:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-tga:$depsTwelveMonkeys")
  implementation("com.twelvemonkeys.imageio:imageio-bmp:$depsTwelveMonkeys")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  downloadSources.set(true)
  instrumentCode.set(true)
  updateSinceUntilBuild.set(true)
//  localPath.set(properties("idePath"))

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
//    plugins.set(
//        listOf(
//            "java",
//            "com.intellij.CloudConfig",
//            "Git4Idea",
//        )
//    )
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  path.set("${project.projectDir}/docs/CHANGELOG.md")
  version.set(properties("pluginVersion"))
  header.set(provider { version.get() })
  itemPrefix.set("-")
  keepUnreleasedSection.set(true)
  unreleasedTerm.set("[Unreleased]")
  groups.set(listOf("Features", "Fixes", "Other", "Bump"))
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
  config.setFrom("./detekt-config.yml")
  buildUponDefaultConfig = true
  autoCorrect = true
  ignoreFailures = true
}

tasks {
  wrapper {
    gradleVersion = properties("gradleVersion")
  }

  properties("javaVersion").let {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
      sourceCompatibility = it
      targetCompatibility = it
    }
    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = it
      kotlinOptions.freeCompilerArgs += listOf("-Xskip-prerelease-check", "-Xjvm-default=all")
    }
  }

  withType<Detekt> {
    jvmTarget = properties("javaVersion")
    reports.xml.required = true
  }

  withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }

  sourceSets {
    main {
      java.srcDirs("src/main/java")
      resources.srcDirs("src/main/resources")
    }
  }

  patchPluginXml {
    version = properties("pluginVersion")
    sinceBuild = properties("pluginSinceBuild")
    untilBuild = properties("pluginUntilBuild")

    // Get the latest available change notes from the changelog file
    changeNotes = changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML)
  }

  runPluginVerifier {
    ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map { it.trim() }.toList())
  }

  buildSearchableOptions {
    enabled = false
  }

//  runIde {
//    jvmArgs = properties("jvmArgs").split("")
//    systemProperty("jb.service.configuration.url", properties("salesUrl"))
//  }

  signPlugin {
    certificateChain = environment("CERTIFICATE_CHAIN")
    privateKey = environment("PRIVATE_KEY")
    password = environment("PRIVATE_KEY_PASSWORD")
  }

  publishPlugin {
    token = environment("PUBLISH_TOKEN")
    channels = listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first())
  }

  runIde {
    ideDir = fileProperties("idePath")
  }

  register("markdownToHtml") {
    val input = File("${project.projectDir}/docs/CHANGELOG.md")
    File("${project.projectDir}/docs/CHANGELOG.html").run {
      writeText(markdownToHTML(input.readText()))
    }
  }
}
