package net.apollofops.first.vendortools.combiner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;

import groovy.ant.FileNameFinder;
import net.apollofops.first.vendortools.VendordepExtension;

public class VendorToolsCombinerPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// Extension dependencies
		PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
		VendordepExtension vendordepExtension = project.getExtensions().getByType(VendordepExtension.class);

		// Project info
		Directory buildDir = project.getLayout().getBuildDirectory().get();
		File productsFolder = project.file(String.format("%s/products", buildDir));

		// Find the artifact files
		FileNameFinder fileFinder = new FileNameFinder();

		ConfigurableFileCollection zipFiles = project.files(fileFinder.getFileNames(productsFolder.getAbsolutePath(), "**/allOutputs/*.zip"));
		ConfigurableFileCollection jarFiles = project.files(fileFinder.getFileNames(productsFolder.getAbsolutePath(), "**/allOutputs/*.jar"));

		ConfigurableFileCollection allFiles = project.files(zipFiles, jarFiles);

		// Get the metadata for the package
		File metadataFile = project.file(fileFinder.getFileNames(productsFolder.getAbsolutePath(), "**/allOutputs/metadata.properties")
				.get(0));
		Properties metadataProperties = new Properties();
		if (metadataFile.exists()) {
			try {
				metadataProperties.load(new FileInputStream(metadataFile));
				vendordepExtension.getReleasesRepoName().set((String) metadataProperties.get("releasesRepoName"));
			} catch (Exception e) {
				System.err.println(e.getStackTrace());
			}
		}

		String regex = "([_M_]*)_GROUP_([^\\.]+)_ID_([^\\.]+)_CLS([^\\.]*).";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

		for (File file : allFiles) {
			Matcher matcher = pattern.matcher(file.getName());

			if (!matcher.find()) {
				continue;
			}

			String groupId = matcher.group(2);
			String artifactId = matcher.group(3);
			String classifier = matcher.group(4);

			String publicationName = String.format("%s%s", groupId, artifactId);

			// Get the publication by name
			MavenPublication publication = publishingExtension.getPublications()
					.withType(MavenPublication.class)
					.findByName(publicationName);

			// If the publication doesn't exist, create one and set it up
			if (publication == null) {
				publication = publishingExtension.getPublications()
						.create(publicationName, MavenPublication.class);
				publication.setArtifactId(artifactId);
				publication.setGroupId(groupId.replace("_", "."));
				publication.setVersion((String) metadataProperties.get("pubVersion"));
			}

			// Add the artifact to the publication
			MavenArtifact artifact = publication.artifact(file);
			if (!classifier.isEmpty()) {
				artifact.setClassifier(classifier.substring(1));
			}
		}
	}
}
