package net.apollofops.vendortools;

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

	@Inject
	public VendordepExtension(Project project) {
		ObjectFactory objects = project.getObjects();

		vendordepJsonFile = objects.fileProperty();
		baseArtifactId = objects.property(String.class);
		artifactGroupId = objects.property(String.class);
		releasesRepoName = objects.property(String.class);
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

	public Provider<String> getBaseNameGroupId() {
		return artifactGroupId.map((groupId) -> groupId.replace(".", "_"));
	}

	public Provider<String> getJavaBaseName() {
		return getBaseNameGroupId().zip(baseArtifactId, (groupId, artifactId) -> String.format("_GROUP_%s_ID_%s-java_CLS", groupId, artifactId));
	}

	public Provider<String> getReleasesRepoUrl(String repoUrl) {
		return getReleasesRepoName().map((repoName) -> String.format("%s/%s", repoUrl, repoName));
	}
}
