package io.github.roboblazers7617.vendortools;

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

public abstract class CopyAllOutputsTask extends DefaultTask {
	private final ConfigurableFileCollection inputFiles;
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
