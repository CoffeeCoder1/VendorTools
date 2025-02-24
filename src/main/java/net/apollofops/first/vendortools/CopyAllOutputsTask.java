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

	/**
	 * Creates a new CopyAllOutputsTask.
	 *
	 * @param objects
	 *                ObjectFactory used to create properties.
	 */
	@Inject
	public CopyAllOutputsTask(ObjectFactory objects) {
		this.inputFiles = objects.fileCollection();
		this.outputsFolder = objects.directoryProperty();
	}

	/**
	 * Gets the {@link #inputFiles} for this task.
	 *
	 * @return
	 *         The {@link #inputFiles} for this task.
	 */
	@InputFiles
	public ConfigurableFileCollection getInputFiles() {
		return inputFiles;
	}

	/**
	 * Gets the {@link #outputsFolder} for this task.
	 *
	 * @return
	 *         The {@link #outputsFolder} for this task.
	 */
	@OutputDirectory
	public DirectoryProperty getOutputsFolder() {
		return outputsFolder;
	}

	/**
	 * Adds a task's outputs to the all outputs task. Reads the task's archiveFile property and adds it to the {@link #inputFiles}.
	 *
	 * @param task
	 *                Task to add.
	 */
	public void addTask(Task task) {
		dependsOn(task);
		inputFiles.from(task.property("archiveFile"));
	}

	/**
	 * Gets the description of this task.
	 *
	 * @return
	 *         The description of this task.
	 */
	@Override
	public String getDescription() {
		return "Copies the output Jars to a directory for later publishing.";
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
	 * Copies the {@link #inputFiles} to the {@link #outputsFolder}.
	 *
	 * @throws IOException
	 *                 If an IOException occurs while writing the file.
	 * @see org.gradle.api.Project#copy(org.gradle.api.Action)
	 */
	@TaskAction
	public void execute() throws IOException {
		getProject().copy(t -> {
			t.from(inputFiles);
			t.into(outputsFolder);
		});
	}
}
