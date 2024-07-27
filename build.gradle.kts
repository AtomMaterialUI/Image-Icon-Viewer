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

fun properties(key: String) = providers.gradleProperty(key).get()

fun environment(key: String) = providers.environmentVariable(key)

fun fileProperties(key: String) = project.findProperty(key).toString().let { if (it.isNotEmpty()) file(it) else null }


plugins {
  signing
  // Java support
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.gradleIntelliJPlugin)
  alias(libs.plugins.changelog)
  alias(libs.plugins.detekt)
  alias(libs.plugins.ktlint)
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

val depsTwelveMonkeys = properties("depsTwelveMonkeys")

// Configure project's dependencies
repositories {
  mavenCentral()
  mavenLocal()
  gradlePluginPortal()

  intellijPlatform {
    defaultRepositories()
    jetbrainsRuntime()
  }
}

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
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

  intellijPlatform {
    intellijIdeaUltimate(platformVersion, useInstaller = false)
    instrumentationTools()
    pluginVerifier()
    zipSigner()
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

kotlin {
  jvmToolchain(17)
}

intellijPlatform {
  pluginConfiguration {
    id = pluginGroup
    name = pluginName
    version = pluginVersion

    ideaVersion {
      sinceBuild = pluginSinceBuild
      untilBuild = pluginUntilBuild
    }

    // changeNotes = provider {
    //   markdownToHTML(File("./docs/RELEASE-NOTES.md").readText())
    // }
  }

  publishing {
    token = environment("PUBLISH_TOKEN")
    channels = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
  }

  signing {
    certificateChain = environment("CERTIFICATE_CHAIN")
    privateKey = environment("PRIVATE_KEY")
    password = environment("PRIVATE_KEY_PASSWORD")
  }
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  path.set("${project.projectDir}/docs/CHANGELOG.md")
  version.set(pluginVersion)
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
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
  }

  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  withType<Detekt> {
    jvmTarget = "17"
  }

  wrapper {
    gradleVersion = properties("gradleVersion")
  }

  register("markdownToHtml") {
    val input = File("${project.projectDir}/docs/CHANGELOG.md")
    File("${project.projectDir}/docs/CHANGELOG.html").run {
      writeText(markdownToHTML(input.readText()))
    }
  }
}
