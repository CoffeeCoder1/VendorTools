package net.apollofops.first.vendortools;

import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.provider.MapProperty;

/**
 * Replaces template variables in a Vendordep JSON file and writes it to the outputs folder.
 */
public abstract class VendordepJsonTask extends DefaultTask {
	/**
	 * The Vendordep JSON file to read.
	 */
	private final RegularFileProperty vendordepFile;
	/**
	 * The directory to output to.
	 */
	private final DirectoryProperty outputsFolder;
	/**
	 * A map of values to substitue into the file.
	 */
	private final MapProperty<String, String> valueMap;

	/**
	 * Creates a new VendordepJsonTask.
	 *
	 * @param objects
	 *                ObjectFactory used to create properties.
	 */
	@Inject
	public VendordepJsonTask(ObjectFactory objects) {
		this.vendordepFile = objects.fileProperty();
		this.outputsFolder = objects.directoryProperty();
		this.valueMap = objects.mapProperty(String.class, String.class);
	}

	/**
	 * Gets the {@link #vendordepFile} for this task.
	 *
	 * @return
	 *         {@link #vendordepFile} for this task.
	 */
	@InputFile
	public RegularFileProperty getVendordepFile() {
		return vendordepFile;
	}

	/**
	 * Gets the {@link #outputsFolder} for this task.
	 *
	 * @return
	 *         {@link #outputsFolder} for this task.
	 */
	@OutputDirectory
	public DirectoryProperty getOutputsFolder() {
		return outputsFolder;
	}

	/**
	 * Gets the {@link #valueMap} for this task.
	 *
	 * @return
	 *         {@link #valueMap} for this task.
	 */
	@Input
	public MapProperty<String, String> getValueMap() {
		return valueMap;
	}

	/**
	 * Gets the description of this task.
	 *
	 * @return
	 *         The description of this task.
	 */
	@Override
	public String getDescription() {
		return "Builds the vendordep JSON file.";
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
	 * Copies the {@link #vendordepFile JSON file} to the {@link #outputsFolder},
	 * filling in template values from the {@link #valueMap} in the process.
	 *
	 * @throws IOException
	 *                 If an IOException occurs while writing the file.
	 * @see org.gradle.api.Project#copy(org.gradle.api.Action)
	 */
	@TaskAction
	public void execute() throws IOException {
		getProject().copy(t -> {
			t.from(vendordepFile.getAsFile().get());
			t.into(outputsFolder);

			// Apply template variables from the vendordep file.
			t.expand(valueMap.get());
		});
	}
}
