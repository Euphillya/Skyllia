package fr.euphyllia.skyllia;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("UnstableApiUsage")
public class SkylliaLoader implements PluginLoader {
    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.euphyllia.moe/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("mojang", "default", "https://libraries.minecraft.net").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());
        resolver.addRepository(new RemoteRepository.Builder("thenextlvl", "default", "https://repo.thenextlvl.net/releases").build());

        // Maven Repository Dependencies
        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.logging.log4j:log4j-core:2.26.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.logging.log4j:log4j-api:2.26.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.electronwill.night-config:toml:3.9.0"), null));

        // HikariCP dependency
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:7.1.0"), null));

        // MariaDB dependency
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.5.9"), null));

        // SQLite dependency
        resolver.addDependency(new Dependency(new DefaultArtifact("org.xerial:sqlite-jdbc:3.53.2.0"), null));

        // PostgreSQL dependency
        resolver.addDependency(new Dependency(new DefaultArtifact("org.postgresql:postgresql:42.7.11"), null));

        // Mojang Repo
        resolver.addDependency(new Dependency(new DefaultArtifact("com.mojang:brigadier:1.0.18"), null));

        // thenextlvl Repo
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.faststats.metrics:bukkit:0.27.0"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
