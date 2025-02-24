package net.apollofops.first.vendortools.java;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import net.apollofops.first.vendortools.CopyAllOutputsTask;
import net.apollofops.first.vendortools.VendorToolsPlugin;
import net.apollofops.first.vendortools.VendordepExtension;

/**
 * {@link Plugin} to facilitate building Java Vendordeps.
 */
public class VendorToolsJavaPlugin implements Plugin<Project> {
	/**
	 * Applies the plugin to the given {@link Project}.
	 *
	 * @param project
	 *                {@link Project} to apply the plugin to.
	 */
	@Override
	public void apply(Project project) {
		// Main plugin extension
		VendordepExtension vendordepExtension = project.getExtensions().getByType(VendordepExtension.class);

		// Plugin dependencies
		project.getPluginManager().apply(JavaPlugin.class);

		// Extension dependencies
		PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
		JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);

		// Task dependencies
		Task buildTask = project.getTasks().getByName("build");
		Task classesTask = project.getTasks().getByName("classes");
		Javadoc javadocTask = project.getTasks().withType(Javadoc.class).getByName("javadoc");
		CopyAllOutputsTask copyAllOutputsTask = project.getTasks().withType(CopyAllOutputsTask.class).getByName("copyAllOutputs");

		Directory buildDir = project.getLayout().getBuildDirectory().get();
		File outputsFolder = project.file(String.format("%s/outputs", buildDir));

		SourceSet mainSourceSet = javaPluginExtension.getSourceSets().getByName("main");

		// Jar tasks
		Jar jarTask = project.getTasks().withType(Jar.class).getByName("jar");

		Jar sourcesJarTask = project.getTasks().register("sourcesJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main source.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveClassifier().set("sources");
			task.from(mainSourceSet.getAllSource());
			task.dependsOn(classesTask);
		}).get();

		Jar javadocJarTask = project.getTasks().register("javadocJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main Javadoc.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveClassifier().set("javadoc");
			task.from(javadocTask.getDestinationDir());
			task.dependsOn(javadocTask);
		}).get();

		Jar outputJarTask = project.getTasks().register("outputJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getBaseName("java"));
			task.getDestinationDirectory().set(outputsFolder);
			task.from(mainSourceSet.getOutput());
			task.dependsOn(classesTask);
		}).get();

		Jar outputSourcesJarTask = project.getTasks().register("outputSourcesJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output sources.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getBaseName("java"));
			task.getArchiveClassifier().set("sources");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(mainSourceSet.getAllSource());
			task.dependsOn(classesTask);
		}).get();

		Jar outputJavadocJarTask = project.getTasks().register("outputJavadocJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output Javadoc.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getBaseName("java"));
			task.getArchiveClassifier().set("javadoc");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(javadocTask.getDestinationDir());
			task.dependsOn(javadocTask);
		}).get();

		// Build artifacts
		project.getArtifacts().add("archives", sourcesJarTask);
		project.getArtifacts().add("archives", javadocJarTask);
		project.getArtifacts().add("archives", outputJarTask);
		project.getArtifacts().add("archives", outputSourcesJarTask);
		project.getArtifacts().add("archives", outputJavadocJarTask);

		// Build outputs
		copyAllOutputsTask.addTask(outputSourcesJarTask);
		copyAllOutputsTask.addTask(outputJavadocJarTask);
		copyAllOutputsTask.addTask(outputJarTask);

		// Build task dependencies
		buildTask.dependsOn("outputSourcesJar");
		buildTask.dependsOn("outputJavadocJar");
		buildTask.dependsOn("outputJar");

		project.afterEvaluate((ae) -> {
			// Publications
			MavenPublication javaPublication = publishingExtension.getPublications().create("java", MavenPublication.class);
			javaPublication.artifact(jarTask);
			javaPublication.artifact(sourcesJarTask);
			javaPublication.artifact(javadocJarTask);
			javaPublication.setArtifactId(String.format("%s-java", vendordepExtension.getBaseArtifactId().get()));
			javaPublication.setGroupId(vendordepExtension.getArtifactGroupId().get());
			javaPublication.setVersion((String) project.getVersion());
		});
	}
}
