import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "u.ways"
version = "SNAPSHOT"

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

plugins {
    /**
     * The Application plugin facilitates creating an executable JVM application for easy dev run
     * See: https://docs.gradle.org/current/userguide/application_plugin.html
     */
    application
    kotlin("jvm") version "1.4.31"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.amshove.kluent:kluent:1.63")

    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

application {
    mainClass.set("u.ways.Main")
}

/**
 * Redirect system.in to the gradle run task so you can capture user input
 *
 * You can also add more options when running the app such as:
 *   -q               runs task in "quiet" mode (to avoid having > Building > :run)
 *   --console=plain  drops execution status: <=-> 80% EXECUTING...
 *
 * Example:
 *   ./gradlew run -q --console=plain --args="ppd -R --mtu 20"
 *
 * See: https://docs.gradle.org/current/userguide/command_line_interface.html
 */
tasks.getByName<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "u.ways.Main"
    }

    /**
     *  Create a fat JAR
     *  See: https://docs.gradle.org/current/userguide/working_with_files.html#sec:creating_uber_jar_example
     */
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "6.6"
    distributionType = Wrapper.DistributionType.ALL
}