package net.apollofops.first.vendortools.cpp;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.bundling.Zip;

import net.apollofops.first.vendortools.CopyAllOutputsTask;
import net.apollofops.first.vendortools.VendorToolsPlugin;
import net.apollofops.first.vendortools.VendordepExtension;

public class VendorToolsCppPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// Main plugin extension
		VendordepExtension vendordepExtension = project.getExtensions().getByType(VendordepExtension.class);

		// Extension dependencies
		ExtraPropertiesExtension extraPropertiesExtension = project.getExtensions().getByType(ExtraPropertiesExtension.class);

		// Task dependencies
		Task buildTask = project.getTasks().getByName("build");
		CopyAllOutputsTask copyAllOutputsTask = project.getTasks().withType(CopyAllOutputsTask.class).getByName("copyAllOutputs");

		File licenseFile = project.file(extraPropertiesExtension.get("licenseFile"));

		Directory buildDir = project.getLayout().getBuildDirectory().get();
		File outputsFolder = project.file(String.format("%s/outputs", buildDir));

		// C++ tasks
		// TODO: Move source configuration to the extension
		Zip cppHeadersZip = project.getTasks().register("cppHeadersZip", Zip.class, task -> {
			task.setDescription("Assembles a Zip archive containing the C++ headers.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getBaseName("cpp"));
			task.getArchiveClassifier().set("headers");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(licenseFile);
			task.from("src/main/native/include");
			task.into("/");
		}).get();

		Zip cppSourceZip = project.getTasks().register("cppSourceZip", Zip.class, task -> {
			task.setDescription("Assembles a Zip archive containing the C++ source.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getBaseName("cpp"));
			task.getArchiveClassifier().set("sources");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(licenseFile);
			task.from("src/main/native/cpp");
			task.into("/");
		}).get();

		Zip cppDriverHeadersZip = project.getTasks().register("cppDriverHeadersZip", Zip.class, task -> {
			task.setDescription("Assembles a Zip archive containing the C++ driver headers.");
			task.setGroup(VendorToolsPlugin.BUILD_TASK_GROUP);

			task.getArchiveBaseName().set(vendordepExtension.getBaseName("driver"));
			task.getArchiveClassifier().set("headers");
			task.getDestinationDirectory().set(outputsFolder);
			task.from(licenseFile);
			task.from("src/main/driver/include");
			task.into("/");
		}).get();

		buildTask.dependsOn(cppHeadersZip);
		buildTask.dependsOn(cppSourceZip);
		buildTask.dependsOn(cppDriverHeadersZip);

		copyAllOutputsTask.addTask(cppHeadersZip);
		copyAllOutputsTask.addTask(cppSourceZip);
		copyAllOutputsTask.addTask(cppDriverHeadersZip);
	}
}
