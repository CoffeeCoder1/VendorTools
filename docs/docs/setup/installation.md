---
layout: default
title: Installation
parent: Setup
---

# Installation
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Installation

Installing the plugin is as simple as adding it to your library's `plugins` block and adding a few extra configuration values.

```gradle
plugins {
    ...
	id 'net.apollofops.first.VendorTools' version "2025.0.0"
}

// For C++
ext {
	licenseFile = "$rootDir/LICENSE.md"
}

...

vendordep {
	baseArtifactId = "MyLibrary" // Artifact ID (the name of the artifacts in the repo)
	artifactGroupId = "com.mycompany.mylibrary" // Group ID
	vendordepJsonFile = file("MyLibrary.json") // JSON file to read from
	releasesRepoName = "mylibrary" // Repo name (subdirectory of the Maven repository used by the Vendordep - allows for multiple Vendordeps to be put in the same repo but still be kept completely separate)
	enableJava = true // Enables Java builds
	enableCpp = true // Enables C++ builds
}
```

## Vendordep JSON

Now, you can make a Vendordep JSON file. VendorTools handles templating things like the library version, so you won't need to update things here for the most part once you've made it.

```json
{
	"fileName": "MyLibrary-${version}.json",
	"name": "MyLibrary",
	"version": "${version}",
	"frcYear": "2025",
	"uuid": "Generate A Unique GUID https://guidgenerator.com/online-guid-generator.aspx and insert it here",
	"mavenUrls": [
		"URL to your maven repo"
	],
	"jsonUrl": "URL to your vendordep JSON",
	"javaDependencies": [
		{
			"groupId": "${groupId}",
			"artifactId": "${artifactId}-java",
			"version": "${version}"
		}
	],
	"jniDependencies": [],
	"cppDependencies": [],
	"requires": []
}
```
