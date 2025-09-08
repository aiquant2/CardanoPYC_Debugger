//plugins {
//    id("java")
//
//    id("org.jetbrains.kotlin.jvm") version "2.1.0"
//    id("org.jetbrains.intellij.platform") version "2.5.0"
//
//}
//
//group = "com.pyc.cardanopyc_debugger    "
//version = "1.0-SNAPSHOT"
//
//repositories {
//    mavenCentral()
//    intellijPlatform {
//        defaultRepositories()
//    }
//}
//
//dependencies {
//    intellijPlatform {
//        // ✅ Use PyCharm Community Edition instead of IC (IntelliJ Community)
//        create("PC", "2024.1.2") // You can change version as needed
//
//        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
//    }
//
//    // ✅ Add JUnit 4 (required by IntelliJ Test Framework)
//    testImplementation("junit:junit:4.13.2")
//    testImplementation("org.mockito:mockito-core:5.12.0")
//    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
//    testImplementation("org.mockito:mockito-core:5.10.0")
//    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
//
//}
//
//intellijPlatform {
//    pluginConfiguration {
//        ideaVersion {
//            sinceBuild = "241" // Matches 2024.1.x
//        }
//
//        changeNotes = """
//          Initial CardanoPyC Debugger for Haskell & Smart Contracts.
//        """.trimIndent()
//    }
//}
//
//tasks {
//    withType<JavaCompile> {
//        sourceCompatibility = "17"
//        targetCompatibility = "17"
//    }
//    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = "21"
//    }
//}
//
//
//
//




// new



plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.8.0" // ✅ updated
}

group = "com.pyc.cardanopyc_debugger"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // ✅ Use PyCharm Community Edition instead of IntelliJ
        create("PC", "2024.1.2")

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    // ✅ JUnit + Mockito
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241" // Matches PyCharm 2024.1.x
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

    // ✅ Migration: use compilerOptions DSL instead of kotlinOptions
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    // ✅ Disable buildSearchableOptions to prevent "Only one instance of PyCharm" error
    buildSearchableOptions {
        enabled = false
    }
    prepareJarSearchableOptions {
        enabled = false
    }
}
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}
