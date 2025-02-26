---
layout: default
title: Maven Repository
parent: Setup
---

# Maven Repository
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

Now that you can build your library, you need to publish it somewhere, and VendorTools has features to make this super easy to set up. The CI workflows included with VendorTools include a few that are designed to allow you to publish Maven packages to a Git repository and host them with GitHub Pages, which makes it very easy to publish Vendordep packages.

We'll make our own here using a template, but a good finished example of these Maven repos can be found at [roboblazers7617/maven-repo](https://github.com/roboblazers7617/maven-repo).

## GitHub App setup

First, you'll need to create a GitHub App to use for authentication in the CI workflows. You can follow [this guide](https://docs.github.com/en/apps/creating-github-apps/registering-a-github-app/registering-a-github-app) to do this. You will need to give the app the following scopes.

* Contents: Read and write
	* Used to call a [repository dispatch](https://docs.github.com/en/rest/repos/repos#create-a-repository-dispatch-event) to trigger a maven repo update.
	* Used to download build output artifacts from library repositories.

Once you've created it, generate a private key and save that to an [organization secret](https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions#creating-secrets-for-an-organization). Give all repositories access to this secret. Also, save your App's App ID to an [organization variable](https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#creating-configuration-variables-for-an-organization). You can also save these to repository secrets and variables, but you'll need them for every repository so I'd recommend just making them organization-wide. Also, if you name the secret `VENDORDEP_BOT_PRIVATE_KEY` and the variable `VENDORDEP_BOT_APP_ID`, the CI workflows in the template repository will work without any extra configuration.

## Repository setup

Now, you'll need to create a Git repository to contain the repo. Create a new repository on GitHub using the [VendorTools Maven template](https://github.com/CoffeeCoder1/VendorTools-maven-template), and [set up Pages to deploy from GitHub Actions](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site#publishing-with-a-custom-github-actions-workflow).

### Gradle setup

The template should work without any changes to `build.gradle` file, since all of the organization-specific metadata about the libraries is provided through environment variables and the output metadata file.

### CI setup

Similarly to the Gradle configuration, the CI workflow should work without any extra configuration as long as you named the app credentials as suggested above, since all of the library-specific metadata is passed in through the client payload and package metadata file. If you want to name the credentials somewhere else, just find every reference to them and change their names to whatever you want to name them.

## Library setup

Now that you've set up the repository, you need to set up a workflow to publish to it. I have put a template below, which should work to build and publish a Java library without any extra configuration (provided you named your secrets as described above).

The publish workflows work as described below.

1. The library's publish workflow is triggered by a push.
2. The library is built, and the outputs (`build/allOutputs` directory) are uploaded as an Artifact. Multiple platforms can be built here in different workflow jobs, to allow for cross-platform C++ builds.
3. The trigger-maven-update action is called, which triggers the update workflow on the Maven repo (only called when a tag is pushed).
4. The update workflow downloads the build artifacts and runs them through the VendorTools combiner.
5. The repo changes are committed and pushed to the Maven repo.
6. The repo is uploaded as an artifact and deployed to GitHub Pages.

The workflow below is a template for the library publish workflow. More publish jobs can be added below the `build-java` job and added to the `update-maven` job's requirements (`needs:`).

{% raw %}
```yml
name: Publish

on: push

jobs:
  # Builds the library and uploads the outputs as an artifact
  build-java:
    uses: CoffeeCoder1/VendorTools/.github/workflows/build-java.yml@2025.0.3

  # Triggers the update workflow on the Maven repo
  update-maven:
    needs: [build-java]
    runs-on: ubuntu-latest

    # Only deploy on tag
    if: startsWith(github.ref, 'refs/tags/')

    steps:
      - name: Generate an authentication token
        uses: actions/create-github-app-token@v1
        id: app-token
        with:
          app-id: ${{ vars.VENDORDEP_BOT_APP_ID }}
          private-key: ${{ secrets.VENDORDEP_BOT_PRIVATE_KEY }}
          # Set the owner, so the token can be used in all repositories
          owner: ${{ github.repository_owner }}

      - name: Trigger update
        uses: CoffeeCoder1/VendorTools/.github/actions/trigger-maven-update@2025.0.3
        with:
          github-token: ${{ steps.app-token.outputs.token }}
          repository-name: ${{ github.repository_owner }}/maven-repo
```
{% endraw %}
