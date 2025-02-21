package net.apollofops.first.vendortools;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

/**
 * An extension used for Vendordep configuration.
 */
public class VendordepExtension {
	/**
	 * The Vendordep JSON file template.
	 */
	private final RegularFileProperty vendordepJsonFile;
	/**
	 * The base artifact ID of the Vendordep.
	 */
	private final Property<String> baseArtifactId;
	/**
	 * The group ID of the Vendordep.
	 */
	private final Property<String> artifactGroupId;
	/**
	 * The name of the maven repo to release to. Appended to the repo URL.
	 */
	private final Property<String> releasesRepoName;
	/**
	 * The URL of the maven repo to release to.
	 */
	private final Property<String> mavenRepoUrl;
	private final Property<Boolean> enableJava;
	private final Property<Boolean> enableCpp;
	private final Property<Boolean> enableCombiner;

	/**
	 * Creates a new VendordepExtension.
	 *
	 * @param project
	 *                The project to apply it to.
	 */
	@Inject
	public VendordepExtension(Project project) {
		ObjectFactory objects = project.getObjects();

		vendordepJsonFile = objects.fileProperty();
		baseArtifactId = objects.property(String.class);
		artifactGroupId = objects.property(String.class);
		releasesRepoName = objects.property(String.class);
		enableJava = objects.property(Boolean.class);
		enableCpp = objects.property(Boolean.class);
		mavenRepoUrl = objects.property(String.class);
		enableCombiner = objects.property(Boolean.class);

		// Defaults
		enableJava.set(false);
		enableCpp.set(false);
		enableCombiner.set(false);
		mavenRepoUrl.set(String.format("%s/repos", project.getRootDir()));
		releasesRepoName.set(System.getenv("releasesRepoName"));
	}

	public RegularFileProperty getVendordepJsonFile() {
		return vendordepJsonFile;
	}

	public Property<String> getBaseArtifactId() {
		return baseArtifactId;
	}

	public Property<String> getArtifactGroupId() {
		return artifactGroupId;
	}

	public Property<String> getReleasesRepoName() {
		return releasesRepoName;
	}

	public Property<String> getMavenRepoUrl() {
		return mavenRepoUrl;
	}

	public Property<Boolean> getEnableJava() {
		return enableJava;
	}

	public Property<Boolean> getEnableCpp() {
		return enableCpp;
	}

	public Property<Boolean> getEnableCombiner() {
		return enableCombiner;
	}

	/**
	 * Gets the group ID used by the base name. This is the regular group ID, but with periods replaced with underscores.
	 *
	 * @return
	 *         A string provider that provides the group ID.
	 */
	public Provider<String> getBaseNameGroupId() {
		return artifactGroupId.map((groupId) -> groupId.replace(".", "_"));
	}

	/**
	 * Gets the base name to use for the artifacts.
	 *
	 * @param artifactClassifier
	 *                The classifier string to use.
	 * @return
	 *         A string provider that provides the base name.
	 */
	public Provider<String> getBaseName(String artifactClassifier) {
		return getBaseNameGroupId().zip(baseArtifactId, (groupId, artifactId) -> String.format("_GROUP_%s_ID_%s-%s_CLS", groupId, artifactId, artifactClassifier));
	}

	/**
	 * Gets the repo URL for the plugin based on the {@link #mavenRepoUrl} and {@link #releasesRepoName}.
	 *
	 * @return
	 *         A string provider that provides the repo URL.
	 */
	public Provider<String> getReleasesRepoUrl() {
		return getReleasesRepoName().zip(mavenRepoUrl, (repoName, repoUrl) -> String.format("%s/%s", repoUrl, repoName));
	}
}
