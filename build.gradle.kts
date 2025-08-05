import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun config(name: String) = project.findProperty(name).toString()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    java
// https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}

group = config("group")
version = config("version")

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        create(config("platformType"), config("platformVersion"))
        compatiblePlugins(providers.gradleProperty("plugins").map { it.split(',') })
        val platformBundledPlugins = providers.gradleProperty("platformBundledPlugins").map { it.split(',') }
        if (platformBundledPlugins.isPresent && platformBundledPlugins.get().isNotEmpty()) {
            bundledPlugins(platformBundledPlugins)
        }

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name.set(config("pluginName"))
        version.set(project.version.toString())
        ideaVersion {
            sinceBuild.set(config("platformSinceBuild"))
            untilBuild.set(provider { null })
        }
    }

    buildSearchableOptions = false

    pluginVerification.ides {
        recommended()
    }

    publishing {
        try {
            token.set(file("token.txt").readLines()[0])
        } catch (_: Exception) {
            println("No token.txt found")
        }
        channels.set(listOf(config("publishChannel")))
    }
}

tasks {
    config("jvmVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(it))
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = config("gradleVersion")
    }

    test {
        useJUnit()

        maxHeapSize = "1G"
    }
}
