package net.apollofops.first.vendortools.combiner;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;

import groovy.ant.FileNameFinder;
import groovy.lang.Closure;
import net.apollofops.first.vendortools.VendorToolsPlugin;
import net.apollofops.first.vendortools.VendordepExtension;

/**
 * {@link Plugin} that combines the outputs from Vendordep builds and publishes
 * them to a Maven repository.
 */
public class VendorToolsCombinerPlugin implements Plugin<Project> {
	/**
	 * Applies the plugin to the given {@link Project}.
	 *
	 * @param project
	 *            {@link Project} to apply the plugin to.
	 */
	@Override
	public void apply(Project project) {
		// Object factory
		ObjectFactory objects = project.getObjects();

		// Extension dependencies
		PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
		VendordepExtension vendordepExtension = project.getExtensions().getByType(VendordepExtension.class);

		// Project info
		Directory buildDir = project.getLayout().getBuildDirectory().get();
		File productsFolder = project.file(String.format("%s/products", buildDir));

		// Find the artifact files
		FileNameFinder fileFinder = new FileNameFinder();

		List<File> outputsFolders = new ArrayList<File>();
		for (File file : project.file(productsFolder).listFiles()) {
			if (file.isDirectory()) {
				outputsFolders.add(file);
			}
		}

		outputsFolders.forEach((folder) -> {
			// Get artifact files
			ConfigurableFileCollection zipFiles = project
					.files(fileFinder.getFileNames(folder.getAbsolutePath(), "**/*.zip"));
			ConfigurableFileCollection jarFiles = project
					.files(fileFinder.getFileNames(folder.getAbsolutePath(), "**/*.jar"));

			ConfigurableFileCollection allFiles = project.files(zipFiles, jarFiles);

			// Metadata
			Property<String> version = objects.property(String.class);
			version.set((String) project.getVersion());
			Property<String> repoName = objects.property(String.class);

			// Get the metadata for the package
			File metadataFile = project.file(fileFinder.getFileNames(folder.getAbsolutePath(), "**/metadata.properties")
					.get(0));
			Properties metadataProperties = new Properties();
			if (metadataFile.exists()) {
				try {
					metadataProperties.load(new FileInputStream(metadataFile));
					version.set((String) metadataProperties.get(VendorToolsPlugin.METADATA_VERSION_KEY));
					repoName.set((String) metadataProperties.get(VendorToolsPlugin.METADATA_RELEASES_REPO_NAME_KEY));
				} catch (Exception e) {
					System.err.println(e.getStackTrace());
				}
			}

			String regex = "([_M_]*)_GROUP_([^\\.]+)_ID_([^\\.]+)_CLS([^\\.]*).";
			Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

			// List of publications
			List<MavenPublication> publications = new ArrayList<MavenPublication>();

			// Add artifact files to the publication
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
					publication.setVersion(version.get());
					publications.add(publication);
				}

				// Add the artifact to the publication
				MavenArtifact artifact = publication.artifact(file);
				if (!classifier.isEmpty()) {
					artifact.setClassifier(classifier.substring(1));
				}
			}

			// Create a Maven repository for this artifact
			MavenArtifactRepository artifactRepository = publishingExtension.getRepositories()
					.maven((repository) -> {
						repository.setName(repoName.get());
						repository.setUrl(repoName.zip(vendordepExtension.getMavenRepoUrl(), (name, url) -> String.format("%s/%s", url, name)));
					});

			// Only publish artifacts to their respective repositories
			project.afterEvaluate((ae) -> {
				ae.getTasks().withType(PublishToMavenRepository.class).configureEach((task) -> {
					Closure<Boolean> repositoryMatch = new Closure<Boolean>(this) {
						@Override
						public Boolean call(Object arguments) {
							// Return true if this task isn't relevant to this publication or if it is going to the correct repository
							return !publications.contains(task.getPublication()) || task.getRepository() == artifactRepository;
						}
					};

					task.onlyIf(repositoryMatch);
				});
			});
		});
	}
}
