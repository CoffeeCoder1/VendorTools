package io.github.roboblazers7617.vendortools;

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

	@Inject
	public OutputVersionsTask(ObjectFactory objects) {
		this.versionFile = objects.fileProperty();
		this.version = objects.property(String.class);
	}

	@Input
	public Property<String> getVersion() {
		return version;
	}

	@OutputFile
	public RegularFileProperty getVersionFile() {
		return versionFile;
	}

	@Override
	public String getDescription() {
		return "Prints the versions of WPILib to a file for use by the downstream packaging project.";
	}

	@Override
	public String getGroup() {
		return "Build";
	}

	@TaskAction
	public void execute() throws IOException {
		ResourceGroovyMethods.write(versionFile.getAsFile().get(), version.get());
	}
}
