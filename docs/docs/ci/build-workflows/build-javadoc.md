---
layout: default
title: Build Javadoc
parent: Build Workflows
---

# Build Javadoc
{: .no_toc }

[Workflow Source]({{ site.github_url }}/blob/main/.github/workflows/build-javadoc.yml){: .btn .btn-purple }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

This workflow builds the library's Javadoc and uploads the outputs as an artifact.

## Sample usage

```yml
jobs:
  build-javadoc:
    uses: CoffeeCoder1/VendorTools/.github/workflows/build-javadoc.yml@2025.0.3
    inputs:
      # This input is used to specify what version to release to the Gradle plugin.
      release-version: # Defaults to the ref name.
      # The name of the artifact to upload.
      artifact-name: # Defaults to publish-outputs-javadoc.
```
