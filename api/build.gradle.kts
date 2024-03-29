dependencies {
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.apache.logging.log4j:log4j-api:2.22.1")
    compileOnly("org.apache.logging.log4j:log4j-core:2.22.1")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
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