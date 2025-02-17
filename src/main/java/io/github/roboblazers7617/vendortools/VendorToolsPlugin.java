package io.github.roboblazers7617.vendortools;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

public abstract class VendorToolsPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(MavenPublishPlugin.class);

		project.setProperty("licenseFile", project.files(String.format("%s/LICENSE.md", project.getRootDir())));
		Directory buildDir = project.getLayout().getBuildDirectory().get();

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
		project.getTasks().register("outputVersions", OutputVersionsTask.class, t -> {
			t.getVersion().set(pubVersion);
			t.getVersionFile().set(versionFile);
		});
		project.getTasks().getByName("build").dependsOn("outputVersions");

		// All outputs task
		project.getTasks().register("copyAllOutputs", CopyAllOutputsTask.class, t -> {
			t.getInputFiles().from(versionFile);
			t.getOutputsFolder().set(allOutputsFolder);
		});
		project.getTasks().getByName("copyAllOutputs").dependsOn("outputVersions");
		project.getTasks().getByName("build").dependsOn("copyAllOutputs");

		// Jar tasks
		project.getTasks().register("sourcesJar", Jar.class, t -> {
			t.setDescription("Assembles a Jar archive containing the main source.");
			t.setGroup("Build");

			t.getArchiveClassifier().set("sources");
			t.from(project.getExtensions()
					.findByType(JavaPluginExtension.class)
					.getSourceSets()
					.getByName("main")
					.getAllSource());
			t.dependsOn(project.getTasks().getByName("classes"));
		});

		project.getTasks().register("javadocJar", Jar.class, t -> {
			t.setDescription("Assembles a Jar archive containing the main Javadoc.");
			t.setGroup("Build");

			t.getArchiveClassifier().set("javadoc");
			t.from(project.getTasks()
					.withType(Javadoc.class)
					.getByName("javadoc")
					.getDestinationDir());
			t.dependsOn(project.getTasks().getByName("javadoc"));
		});

		project.getTasks().register("outputJar", Jar.class, t -> {
			t.setDescription("Assembles a Jar archive containing the main output.");
			t.setGroup("Build");

			t.getArchiveBaseName().set(javaBaseName);
			t.getDestinationDirectory().set(outputsFolder);
			t.from(project.getExtensions()
					.findByType(JavaPluginExtension.class)
					.getSourceSets()
					.getByName("main")
					.getOutput());
			t.dependsOn(project.getTasks().getByName("classes"));
		});

		project.getTasks().register("outputSourcesJar", Jar.class, t -> {
			t.setDescription("Assembles a Jar archive containing the main output sources.");
			t.setGroup("Build");

			t.getArchiveBaseName().set(javaBaseName);
			t.getArchiveClassifier().set("sources");
			t.getDestinationDirectory().set(outputsFolder);
			t.from(project.getExtensions()
					.findByType(JavaPluginExtension.class)
					.getSourceSets()
					.getByName("main")
					.getAllSource());
			t.dependsOn(project.getTasks().getByName("classes"));
		});

		project.getTasks().register("outputJavadocJar", Jar.class, t -> {
			t.setDescription("Assembles a Jar archive containing the main output Javadoc.");
			t.setGroup("Build");

			t.getArchiveBaseName().set(javaBaseName);
			t.getArchiveClassifier().set("javadoc");
			t.getDestinationDirectory().set(outputsFolder);
			t.from(project.getTasks()
					.withType(Javadoc.class)
					.getByName("javadoc")
					.getDestinationDir());
			t.dependsOn(project.getTasks().getByName("classes"));
		});

		project.getTasks().register("vendordepJson", VendordepJsonTask.class, t -> {
			t.getVendordepFile().set(vendordepFile);
			t.getOutputsFolder().set(outputsFolder);
			t.getValueMap().put("version", pubVersion);
			t.getValueMap().put("groupId", artifactGroupId);
			t.getValueMap().put("artifactId", baseArtifactId);
		});

		// Build artifacts
		project.getArtifacts().add("archives", project.getTasks().getByName("sourcesJar"));
		project.getArtifacts().add("archives", project.getTasks().getByName("javadocJar"));
		project.getArtifacts().add("archives", project.getTasks().getByName("outputJar"));
		project.getArtifacts().add("archives", project.getTasks().getByName("outputSourcesJar"));
		project.getArtifacts().add("archives", project.getTasks().getByName("outputJavadocJar"));

		// Build outputs
		project.getTasks().withType(CopyAllOutputsTask.class).all((task) -> task.addTask(project.getTasks().getByName("outputSourcesJar")));
		project.getTasks().withType(CopyAllOutputsTask.class).all((task) -> task.addTask(project.getTasks().getByName("outputJavadocJar")));
		project.getTasks().withType(CopyAllOutputsTask.class).all((task) -> task.addTask(project.getTasks().getByName("outputJar")));

		// Build task dependencies
		project.getTasks().getByName("build").dependsOn("outputSourcesJar");
		project.getTasks().getByName("build").dependsOn("outputJavadocJar");
		project.getTasks().getByName("build").dependsOn("outputJar");

		// Maven repository
		project.getExtensions()
				.getByType(PublishingExtension.class)
				.getRepositories()
				.maven((repository) -> {
					repository.setUrl(releasesRepoUrl);
				});

		// clean {
		// delete releasesRepoUrl
		// }

		// Publications
		MavenPublication javaPublication = project.getExtensions()
				.getByType(PublishingExtension.class)
				.getPublications()
				.create("java", MavenPublication.class);
		javaPublication.artifact(project.getTasks().getByName("jar"));
		javaPublication.artifact(project.getTasks().getByName("sourcesJar"));
		javaPublication.artifact(project.getTasks().getByName("javadocJar"));
		javaPublication.setArtifactId(String.format("%s-java", baseArtifactId));
		javaPublication.setGroupId(artifactGroupId);
		javaPublication.setVersion(pubVersion);
	}
}
