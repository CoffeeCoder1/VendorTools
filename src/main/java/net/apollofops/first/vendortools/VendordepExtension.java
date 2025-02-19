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
	 * The name of the repo to release to.
	 */
	private final Property<String> releasesRepoName;
	private final Property<Boolean> enableJava;
	private final Property<Boolean> enableCpp;

	/**
	 * Creates a new VendordepExtension.
	 *
	 * @param project
	 *            The project to apply it to.
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

		// Defaults
		enableJava.set(false);
		enableCpp.set(false);
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

	public Property<Boolean> getEnableJava() {
		return enableJava;
	}

	public Property<Boolean> getEnableCpp() {
		return enableCpp;
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
	 *            The classifier string to use.
	 * @return
	 *         A string provider that provides the base name.
	 */
	public Provider<String> getBaseName(String artifactClassifier) {
		return getBaseNameGroupId().zip(baseArtifactId, (groupId, artifactId) -> String.format("_GROUP_%s_ID_%s-%s_CLS", groupId, artifactId, artifactClassifier));
	}

	public Provider<String> getReleasesRepoUrl(String repoUrl) {
		return getReleasesRepoName().map((repoName) -> String.format("%s/%s", repoUrl, repoName));
	}
}
