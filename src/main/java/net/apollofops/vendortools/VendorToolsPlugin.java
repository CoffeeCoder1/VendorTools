package net.apollofops.vendortools;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import net.apollofops.vendortools.java.VendorToolsJavaPlugin;

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
		project.getPluginManager().apply(MavenPublishPlugin.class);

		// Extension dependencies
		PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

		// Task dependencies
		Task buildTask = project.getTasks().getByName("build");

		// Project info
		Directory buildDir = project.getLayout().getBuildDirectory().get();

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

		// Vendordep JSON templating
		project.getTasks().register("vendordepJson", VendordepJsonTask.class, task -> {
			task.getVendordepFile().set(vendordepExtension.getVendordepJsonFile());
			task.getOutputsFolder().set(outputsFolder);
			task.getValueMap().put("version", pubVersion);
			task.getValueMap().put("groupId", vendordepExtension.getArtifactGroupId());
			task.getValueMap().put("artifactId", vendordepExtension.getBaseArtifactId());
		});

		// clean {
		// delete releasesRepoUrl
		// }

		// Library build plugins
		project.getPluginManager().apply(VendorToolsJavaPlugin.class);

		project.afterEvaluate((ae) -> {
			// Maven repository
			publishingExtension.getRepositories()
					.maven((repository) -> {
						repository.setUrl(vendordepExtension.getReleasesRepoUrl(repoUrl));
					});
		});
	}
}
