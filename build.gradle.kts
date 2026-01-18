import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun config(name: String) = project.findProperty(name).toString()

repositories {
    mavenLocal()
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    java
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    id("dev.meanmail.intellij-plugin-conventions") version "1.0.0"
    id("dev.meanmail.resumable-download") version "1.0.0"
}

group = config("group")
version = config("version")

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        create(config("platformType"), config("platformVersion"))
        val plugins = providers.gradleProperty("plugins").map { it.split(',') }
        if (plugins.isPresent && plugins.get().isNotEmpty()) {
            compatiblePlugins(plugins)
        }
        val platformBundledPlugins = providers.gradleProperty("platformBundledPlugins").map { it.split(',') }
        if (platformBundledPlugins.isPresent && platformBundledPlugins.get().isNotEmpty()) {
            bundledPlugins(platformBundledPlugins)
        }

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

// Check if runIde task is requested (for optional=true during testing)
val isRunIde = gradle.startParameter.taskNames.any {
    it.contains("runIde", ignoreCase = true) || it.contains("prepareSandbox", ignoreCase = true)
}

intellijPlatform {
    pluginConfiguration {
        name.set(config("pluginName"))
        version.set(project.version.toString())

        ideaVersion {
            sinceBuild.set(config("platformSinceBuild"))
            untilBuild.set(provider { null })
        }
        val pluginCode = config("code")
        if (pluginCode.isNotBlank()) {
            productDescriptor {
                code = pluginCode
                releaseDate = config("releaseDate")
                releaseVersion = config("releaseVersion")
                optional = if (isRunIde) true else config("optional").toBoolean()
            }
        }
    }
    autoReload = false

    buildSearchableOptions = false

    pluginVerification.ides {
        recommended()
    }

    // Enable IDE caching for plugin verification
    // Cache path is configured via org.jetbrains.intellij.platform.intellijPlatformCache in ~/.gradle/gradle.properties
    caching.ides {
        enabled = true
    }

    signing {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(
            providers.environmentVariable("PUBLISH_TOKEN").orElse(
                providers.provider {
                    try {
                        file("token.txt").readLines()[0]
                    } catch (_: Exception) {
                        println("No PUBLISH_TOKEN env variable or token.txt found")
                        ""
                    }
                }
            ))
        channels.set(listOf(config("publishChannel")))
    }
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = config("gradleVersion")
    }

    test {
        useJUnit()

        maxHeapSize = "1G"
    }
}
