plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "me.aymanisam"
version = "1.9.0-beta"
val spigotAPIVersion = "1.20"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotAPIVersion-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.14.0")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("fr.mrmicky:fastboard:2.1.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.shadowJar {
    archiveFileName.set("Hungergames-$version.jar")
    relocate("org.bstats", "$group.bstats")
    relocate("fr.mrmicky.fastboard", "$group.fastboard")
    archiveClassifier.set("")
    destinationDirectory.set(file(System.getenv("OUTPUT_DIR")))
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}