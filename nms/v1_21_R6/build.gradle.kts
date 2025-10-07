plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}


paperweight {
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT") {
        exclude(group = "io.papermc.adventure", module = "adventure-api")
        exclude(group = "net.kyori", module = "adventure-text-minimessage")
        exclude(group = "net.kyori", module = "adventure-api")
    }
    compileOnly(project(":nms:v1_21_R5"))
    compileOnly(project(":api"))

}


tasks {
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
