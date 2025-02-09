plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.aquestry"
version = "1.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.18.0")
    implementation("org.spongepowered:configurate-hocon:4.1.2")
    implementation("com.github.mwiede:jsch:0.2.21")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    shadowJar {
        archiveBaseName.set("nebula")
        archiveClassifier.set("")
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}