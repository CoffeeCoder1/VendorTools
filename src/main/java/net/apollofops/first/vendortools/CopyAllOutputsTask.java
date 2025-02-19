package net.apollofops.first.vendortools;

import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * Copies the outputs from a set of tasks into a single directory.
 */
public abstract class CopyAllOutputsTask extends DefaultTask {
	/**
	 * The input files to copy.
	 */
	private final ConfigurableFileCollection inputFiles;
	/**
	 * The directory to copy into.
	 */
	private final DirectoryProperty outputsFolder;

	@Inject
	public CopyAllOutputsTask(ObjectFactory objects) {
		this.inputFiles = objects.fileCollection();
		this.outputsFolder = objects.directoryProperty();
	}

	@InputFiles
	public ConfigurableFileCollection getInputFiles() {
		return inputFiles;
	}

	@OutputDirectory
	public DirectoryProperty getOutputsFolder() {
		return outputsFolder;
	}

	/**
	 * Adds a task's outputs to the all outputs task. Reads the task's archiveFile property and adds it to the {@link #inputFiles}.
	 *
	 * @param task
	 *            Task to add.
	 */
	public void addTask(Task task) {
		dependsOn(task);
		inputFiles.from(task.property("archiveFile"));
	}

	@Override
	public String getDescription() {
		return "Copies the output Jars to a directory for later publishing.";
	}

	@Override
	public String getGroup() {
		return "Build";
	}

	@TaskAction
	public void execute() throws IOException {
		getProject().copy(t -> {
			t.from(inputFiles);
			t.into(outputsFolder);
		});
	}
}
