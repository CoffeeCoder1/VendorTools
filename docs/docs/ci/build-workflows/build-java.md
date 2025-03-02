---
layout: default
title: Build Java
parent: Build Workflows
---

# Build Java
{: .no_toc }

[Workflow Source]({{ site.github_url }}/blob/main/.github/workflows/build-java.yml){: .btn .btn-purple }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

This workflow builds the library for Java and uploads the outputs as an artifact.

## Sample usage

```yml
jobs:
  build-java:
    uses: CoffeeCoder1/VendorTools/.github/workflows/build-java.yml@2025.0.3
    inputs:
      # This input is used to specify what version to release to the Gradle plugin.
      release-version: # Defaults to the ref name.
      # The name of the artifact to upload.
      artifact-name: # Defaults to build-outputs-java.
```
