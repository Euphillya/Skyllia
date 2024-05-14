plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.1" // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
}


dependencies {
    paperweight.foliaDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.Euphillya:Energie:1.2.0")
    compileOnly("com.github.Euphillya:SGBD-MariaDB:3827fafa25")
}

group = "fr.euphyllia.skyllia";
version = "1.0-RC7.1";

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Euphillya/Skyllia")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
    publications {
        create("gpr", MavenPublication::class) {
            from(components["java"])
        }
    }
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}