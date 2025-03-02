package net.apollofops.first.vendortools;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.WriteProperties;

import net.apollofops.first.vendortools.cpp.VendorToolsCppPlugin;
import net.apollofops.first.vendortools.java.VendorToolsJavaPlugin;
import net.apollofops.first.vendortools.combiner.VendorToolsCombinerPlugin;

/**
 * Main {@link Plugin} of the VendorTools Gradle build tools.
 */
public abstract class VendorToolsPlugin implements Plugin<Project> {
	/**
	 * The group for the build tasks created by this plugin.
	 */
	public static final String BUILD_TASK_GROUP = "Build";
	/**
	 * The key used for the version in the metadata file.
	 */
	public static final String METADATA_VERSION_KEY = "pubVersion";
	/**
	 * The key used for the releases repo name in the metadata file.
	 */
	public static final String METADATA_RELEASES_REPO_NAME_KEY = "releasesRepoName";

	/**
	 * Applies the plugin to the given {@link Project}.
	 *
	 * @param project
	 *                {@link Project} to apply the plugin to.
	 */
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
		if (releaseVersion != null) {
			project.setVersion(releaseVersion);
		} else {
			System.err.println("releaseVersion environment variable not found. Using the version from the project settings.");
		}

		File outputsFolder = project.file(String.format("%s/outputs", buildDir));
		File allOutputsFolder = project.file(String.format("%s/allOutputs", buildDir));
		File metadataFile = project.file(String.format("%s/metadata.properties", outputsFolder));

		// Metadata output
		WriteProperties writePropertiesTask = project.getTasks().register("outputMetadata", WriteProperties.class, task -> {
			task.property(METADATA_VERSION_KEY, project.getVersion());
			task.property(METADATA_RELEASES_REPO_NAME_KEY, vendordepExtension.getReleasesRepoName());
			task.getDestinationFile().set(metadataFile);
		}).get();
		buildTask.dependsOn(writePropertiesTask);

		// All outputs task
		CopyAllOutputsTask copyAllOutputsTask = project.getTasks().register("copyAllOutputs", CopyAllOutputsTask.class, task -> {
			task.getInputFiles().from(metadataFile);
			task.getOutputsFolder().set(allOutputsFolder);
		}).get();
		copyAllOutputsTask.dependsOn(writePropertiesTask);
		buildTask.dependsOn(copyAllOutputsTask);

		// Vendordep JSON templating
		project.getTasks().register("vendordepJson", VendordepJsonTask.class, task -> {
			task.getVendordepFile().set(vendordepExtension.getVendordepJsonFile());
			task.getOutputsFolder().set(outputsFolder);
			task.getValueMap().put("version", (String) project.getVersion());
			task.getValueMap().put("groupId", vendordepExtension.getArtifactGroupId());
			task.getValueMap().put("artifactId", vendordepExtension.getBaseArtifactId());
		});

		project.afterEvaluate((ae) -> {
			// Library build plugins
			if (vendordepExtension.getEnableJava().get()) {
				project.getPluginManager().apply(VendorToolsJavaPlugin.class);
			}
			if (vendordepExtension.getEnableCpp().get()) {
				project.getPluginManager().apply(VendorToolsCppPlugin.class);
			}
			if (vendordepExtension.getEnableCombiner().get()) {
				project.getPluginManager().apply(VendorToolsCombinerPlugin.class);
			}

			// Maven repository
			publishingExtension.getRepositories()
					.maven((repository) -> {
						repository.setUrl(vendordepExtension.getReleasesRepoUrl());
					});
		});
	}
}
