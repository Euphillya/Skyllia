plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

// Todo : I don't think we'll need it anymore since Minecraft is no longer obfuscated.
//paperweight {
//    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
//}

dependencies {
    paperweight.foliaDevBundle("1.21.11-R0.1-SNAPSHOT")//paperweight.paperDevBundle("26.1-R0.1-SNAPSHOT")
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
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}