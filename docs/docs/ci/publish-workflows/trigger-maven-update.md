---
layout: default
title: Publish
parent: Publish Workflows
---

# Trigger Maven Update
{: .no_toc }

[Action Source]({{ site.github_url }}/tree/main/.github/actions/trigger-maven-update){: .btn .btn-purple }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

This Action calls a repository dispatch event on the provided repository and is used to trigger a Maven update, passing in the required information in the process.

## Sample usage

```yml
- name: Trigger update
  uses: CoffeeCoder1/VendorTools/.github/actions/trigger-maven-update@2025.0.3
  with:
    # The GitHub token used to authenticate with the GitHub API. Must have write access to the repository that you are calling.
    github-token: # Required
    # # The name of the GitHub repository to call an update on.
    repository-name: # Required
    # The name of the Github repository to pull build artifacts from.
    artifact-repository-name: # Defaults to this repository.
    # The ID of the workflow run where the desired download artifact was uploaded from.
    artifact-run-id: # Defaults to this workflow run.
    # A glob pattern to the artifacts that should be downloaded.
    artifact-pattern: # Defaults to build-outputs-*
```
