package fr.euphyllia.skyllia;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class SkylliaLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("mojang", "default", "https://libraries.minecraft.net").build());

        // Maven Repo
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:6.2.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.5.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.logging.log4j:log4j-core:2.24.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.logging.log4j:log4j-api:2.24.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.google.guava:guava:33.3.1-jre"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("net.kyori:adventure-text-minimessage:4.17.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.electronwill.night-config:toml:3.8.1"), null));

        // Mojang Repo
        resolver.addDependency(new Dependency(new DefaultArtifact("com.mojang:brigadier:1.0.18"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
