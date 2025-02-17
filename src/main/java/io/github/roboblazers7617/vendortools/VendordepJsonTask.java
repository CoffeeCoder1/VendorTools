package io.github.roboblazers7617.vendortools;

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

public abstract class VendordepJsonTask extends DefaultTask {
	private final RegularFileProperty vendordepFile;
	private final DirectoryProperty outputsFolder;
	private final MapProperty<String, String> valueMap;

	@Inject
	public VendordepJsonTask(ObjectFactory objects) {
		this.vendordepFile = objects.fileProperty();
		this.outputsFolder = objects.directoryProperty();
		this.valueMap = objects.mapProperty(String.class, String.class);
	}

	@InputFile
	public RegularFileProperty getVendordepFile() {
		return vendordepFile;
	}

	@OutputDirectory
	public DirectoryProperty getOutputsFolder() {
		return outputsFolder;
	}

	@Input
	public MapProperty<String, String> getValueMap() {
		return valueMap;
	}

	@Override
	public String getDescription() {
		return "Builds the vendordep JSON file.";
	}

	@Override
	public String getGroup() {
		return "Build";
	}

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
