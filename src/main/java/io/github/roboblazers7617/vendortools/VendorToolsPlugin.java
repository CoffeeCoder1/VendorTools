package io.github.roboblazers7617.vendortools;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ExtraPropertiesExtension;
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
		// Plugin dependencies
		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(MavenPublishPlugin.class);

		// Extension dependencies
		PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
		JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);
		ExtraPropertiesExtension extraPropertiesExtension = project.getExtensions().findByType(ExtraPropertiesExtension.class);

		// Task dependencies
		Task buildTask = project.getTasks().getByName("build");
		Javadoc javadocTask = project.getTasks().withType(Javadoc.class).getByName("javadoc");
		Task classesTask = project.getTasks().getByName("classes");

		// Project info
		Directory buildDir = project.getLayout().getBuildDirectory().get();
		SourceSet mainSourceSet = javaPluginExtension.getSourceSets().getByName("main");

		extraPropertiesExtension.set("licenseFile", project.files(String.format("%s/LICENSE.md", project.getRootDir())));

		// Vendordep configuration
		String releaseVersion = System.getenv("releaseVersion");
		String pubVersion;
		if (releaseVersion == null) {
			pubVersion = "test";
		} else {
			pubVersion = releaseVersion;
		}

		String repoUrl = System.getenv("mavenRepo");
		if (repoUrl == null) {
			repoUrl = String.format("%s/repos", project.getRootDir());
		}

		String releasesRepoUrl = String.format("%s/%s", repoUrl, project.property("vendordepPublish.releasesRepoName"));
		File outputsFolder = project.file(String.format("%s/outputs", buildDir));
		File allOutputsFolder = project.file(String.format("%s/allOutputs", buildDir));
		File versionFile = project.file(String.format("%s/version.txt", outputsFolder));
		File vendordepFile = project.file(project.property("vendordepPublish.vendordepFileName"));

		// Repo artifact configuration
		String artifactGroupId = (String) project.property("vendordepPublish.artifactGroupId");
		String baseArtifactId = (String) project.property("vendordepPublish.baseArtifactId");
		String baseNameGroupId = artifactGroupId.replace(".", "_");
		String javaBaseName = String.format("_GROUP_%s_ID_%s-java_CLS", baseNameGroupId, baseArtifactId);

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

			task.getArchiveBaseName().set(javaBaseName);
			task.getDestinationDirectory().set(outputsFolder);
			task.from(mainSourceSet.getOutput());
			task.dependsOn(classesTask);
		}).get();

		Jar outputSourcesJarTask = project.getTasks().register("outputSourcesJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output sources.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(javaBaseName);
			task.getArchiveClassifier().set("sources");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(mainSourceSet.getAllSource());
			task.dependsOn(classesTask);
		}).get();

		Jar outputJavadocJarTask = project.getTasks().register("outputJavadocJar", Jar.class, task -> {
			task.setDescription("Assembles a Jar archive containing the main output Javadoc.");
			task.setGroup(BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(javaBaseName);
			task.getArchiveClassifier().set("javadoc");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(javadocTask.getDestinationDir());
			task.dependsOn(classesTask);
		}).get();

		project.getTasks().register("vendordepJson", VendordepJsonTask.class, task -> {
			task.getVendordepFile().set(vendordepFile);
			task.getOutputsFolder().set(outputsFolder);
			task.getValueMap().put("version", pubVersion);
			task.getValueMap().put("groupId", artifactGroupId);
			task.getValueMap().put("artifactId", baseArtifactId);
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

		// Maven repository
		publishingExtension.getRepositories()
				.maven((repository) -> {
					repository.setUrl(releasesRepoUrl);
				});

		// clean {
		// delete releasesRepoUrl
		// }

		// Publications
		MavenPublication javaPublication = publishingExtension.getPublications().create("java", MavenPublication.class);
		javaPublication.artifact(jarTask);
		javaPublication.artifact(sourcesJarTask);
		javaPublication.artifact(javadocJarTask);
		javaPublication.setArtifactId(String.format("%s-java", baseArtifactId));
		javaPublication.setGroupId(artifactGroupId);
		javaPublication.setVersion(pubVersion);
	}
}
