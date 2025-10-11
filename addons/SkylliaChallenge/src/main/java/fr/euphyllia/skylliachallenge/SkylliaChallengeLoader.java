package fr.euphyllia.skylliachallenge;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class SkylliaChallengeLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("mojang", "default", "https://libraries.minecraft.net").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());

        // Mojang Repo
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.triumphteam:triumph-gui:3.1.13"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
