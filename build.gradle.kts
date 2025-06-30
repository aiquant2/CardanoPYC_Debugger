

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.pyc.cardanopyc_debugger    "
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // ✅ Use PyCharm Community Edition instead of IC (IntelliJ Community)
        create("PC", "2024.1.2") // You can change version as needed

        // If you need Python plugin support, add this:
        // bundledPlugin("PythonCore")

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241" // Matches 2024.1.x
        }

        changeNotes = """
          Initial CardanoPyC Debugger for Haskell & Smart Contracts.
        """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }
}


// new

