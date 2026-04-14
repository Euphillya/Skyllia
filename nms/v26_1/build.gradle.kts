plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly(project(":nms:v1_21_R7"))
    compileOnly(project(":api"))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

configurations.all {
    // Temps fix - Could not find net.kyori:adventure-text-serializer-ansi:.
    exclude(group = "net.kyori", module = "adventure-text-serializer-ansi")
}
