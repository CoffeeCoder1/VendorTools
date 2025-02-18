package net.apollofops.vendortools;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

public abstract class VendorToolsPlugin implements Plugin<Project> {
	/**
	 * The group for the build tasks created by this plugin.
	 */
	public static final String BUILD_TASK_GROUP = "Build";

	@Override
	public void apply(Project project) {
		// Plugin extension
		VendordepExtension vendordepExtension = project.getExtensions().create("vendordep", VendordepExtension.class, project);

		// Plugin dependencies
		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(MavenPublishPlugin.class);

		// Extension dependencies
		PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
		JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);

		// Task dependencies
		Task buildTask = project.getTasks().getByName("build");
		Javadoc javadocTask = project.getTasks().withType(Javadoc.class).getByName("javadoc");
		Task classesTask = project.getTasks().getByName("classes");

		// Project info
		Directory buildDir = project.getLayout().getBuildDirectory().get();
		SourceSet mainSourceSet = javaPluginExtension.getSourceSets().getByName("main");

		// Vendordep configuration
		String releaseVersion = System.getenv("releaseVersion");
		final String pubVersion;
		if (releaseVersion == null) {
			pubVersion = "test";
		} else {
			pubVersion = releaseVersion;
		}

		String mavenRepo = System.getenv("mavenRepo");
		final String repoUrl;
		if (mavenRepo == null) {
			repoUrl = String.format("%s/repos", project.getRootDir());
		} else {
			repoUrl = mavenRepo;
		}

		File outputsFolder = project.file(String.format("%s/outputs", buildDir));
		File allOutputsFolder = project.file(String.format("%s/allOutputs", buildDir));
		File versionFile = project.file(String.format("%s/version.txt", outputsFolder));

		// Version output
		project.getTasks().register("outputVersions", OutputVersionsTask.class, task -> {
			task.getVersion().set(pubVersion);
			task.getVersionFile().set(versionFile);
		});
		buildTask.dependsOn("outputVersions");

		// All outputs task
		CopyAllOutputsTask copyAllOutputsTask = project.getTasks().register("copyAllOutputs", CopyAllOutputsTask.class, task -> {
			task.getInputFiles().from(versionFile);
			task.getOutputsFolder().set(allOutputsFolder);
		}).get();
		copyAllOutputsTask.dependsOn("outputVersions");
		buildTask.dependsOn("copyAllOutputs");

		// Jar tasks
		Jar jarTask = project.getTasks().withType(Jar.class).getByName("jar");

		Jar sourcesJarTask = project.getTasks().register("sourcesJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main source.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveClassifier().set("sources");
			task.from(mainSourceSet.getAllSource());
			task.dependsOn(classesTask);
		}).get();

		Jar javadocJarTask = project.getTasks().register("javadocJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main Javadoc.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveClassifier().set("javadoc");
			task.from(javadocTask.getDestinationDir());
			task.dependsOn(javadocTask);
		}).get();

		Jar outputJarTask = project.getTasks().register("outputJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getJavaBaseName());
			task.getDestinationDirectory().set(outputsFolder);
			task.from(mainSourceSet.getOutput());
			task.dependsOn(classesTask);
		}).get();

		Jar outputSourcesJarTask = project.getTasks().register("outputSourcesJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output sources.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getJavaBaseName());
			task.getArchiveClassifier().set("sources");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(mainSourceSet.getAllSource());
			task.dependsOn(classesTask);
		}).get();

		Jar outputJavadocJarTask = project.getTasks().register("outputJavadocJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output Javadoc.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getJavaBaseName());
			task.getArchiveClassifier().set("javadoc");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(javadocTask.getDestinationDir());
			task.dependsOn(javadocTask);
		}).get();

		project.getTasks().register("vendordepJson", VendordepJsonTask.class, task -> {
			task.getVendordepFile().set(vendordepExtension.getVendordepJsonFile());
			task.getOutputsFolder().set(outputsFolder);
			task.getValueMap().put("version", pubVersion);
			task.getValueMap().put("groupId", vendordepExtension.getArtifactGroupId());
			task.getValueMap().put("artifactId", vendordepExtension.getBaseArtifactId());
		});

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

		// clean {
		// delete releasesRepoUrl
		// }

		project.afterEvaluate((ae) -> {
			// Maven repository
			publishingExtension.getRepositories()
					.maven((repository) -> {
						repository.setUrl(vendordepExtension.getReleasesRepoUrl(repoUrl));
					});

			// Publications
			MavenPublication javaPublication = publishingExtension.getPublications().create("java", MavenPublication.class);
			javaPublication.artifact(jarTask);
			javaPublication.artifact(sourcesJarTask);
			javaPublication.artifact(javadocJarTask);
			javaPublication.setArtifactId(String.format("%s-java", vendordepExtension.getBaseArtifactId().get()));
			javaPublication.setGroupId(vendordepExtension.getArtifactGroupId().get());
			javaPublication.setVersion(pubVersion);
		});
	}
}
