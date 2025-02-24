package net.apollofops.first.vendortools;

import java.io.IOException;

import javax.inject.Inject;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Prints the versions of WPILib to a file for later use when publishing.
 */
public abstract class OutputVersionsTask extends DefaultTask {
	/**
	 * The project version to write to the file.
	 */
	private final Property<String> version;
	/**
	 * The file to output to.
	 */
	private final RegularFileProperty versionFile;

	/**
	 * Creates a new OutputVersionsTask.
	 *
	 * @param objects
	 *                ObjectFactory used to create properties.
	 */
	@Inject
	public OutputVersionsTask(ObjectFactory objects) {
		this.versionFile = objects.fileProperty();
		this.version = objects.property(String.class);
	}

	/**
	 * Gets the {@link #version} for this task.
	 *
	 * @return
	 *         The {@link #version} for this task.
	 */
	@Input
	public Property<String> getVersion() {
		return version;
	}

	/**
	 * Gets the {@link #versionFile} for this task.
	 *
	 * @return
	 *         The {@link #versionFile} for this task.
	 */
	@OutputFile
	public RegularFileProperty getVersionFile() {
		return versionFile;
	}

	/**
	 * Gets the description of this task.
	 *
	 * @return
	 *         The description of this task.
	 */
	@Override
	public String getDescription() {
		return "Prints the versions of WPILib to a file for use by the downstream packaging project.";
	}

	/**
	 * Gets the group of this task.
	 *
	 * @return
	 *         The group of this task.
	 */
	@Override
	public String getGroup() {
		return "Build";
	}

	/**
	 * Writes the {@link #version} to the {@link #versionFile}.
	 *
	 * @throws IOException
	 *                 If an IOException occurs while writing the file.
	 * @see ResourceGroovyMethods#write(java.io.File, String)
	 */
	@TaskAction
	public void execute() throws IOException {
		ResourceGroovyMethods.write(versionFile.getAsFile().get(), version.get());
	}
}
