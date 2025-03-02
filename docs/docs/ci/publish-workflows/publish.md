---
layout: default
title: Publish
parent: Publish Workflows
---

# Publish
{: .no_toc }

[Action Source]({{ site.github_url }}/tree/main/.github/actions/publish){: .btn .btn-purple }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

This Action downloads the build output artifacts caught by the provided glob pattern and runs them through the combiner, publishing to the local Maven repository.

## Sample usage

```yml
- uses: CoffeeCoder1/VendorTools/.github/actions/publish@2025.0.3
  with:
    # A glob pattern to the artifacts that should be downloaded.
    artifact-pattern: # Defaults to build-outputs-*
    # The repository owner and the repository name joined together by "/".
    artifact-repository: # Defaults to the current repository
    # The ID of the workflow run where the desired download artifact was uploaded from.
    artifact-run-id: # Defaults to this workflow run
    # The GitHub token used to authenticate with the GitHub API. Must have read access to the repository that you are downloading artifacts from.
    github-token: # If not provided, the download-artifacts action defaults to the token for this run (can only download artifacts from the same run).
```
