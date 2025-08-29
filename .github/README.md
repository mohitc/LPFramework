# Workflow Summary

This directory contains the GitHub Actions workflows for automating the build,
test, and publishing processes for this project.

### `publish-dev-env-fedora.yml`

This workflow builds and publishes the custom Docker development environment
image (`dev-env-fedora`) to the GitHub Container Registry (`ghcr.io`). This
image contains all the necessary dependencies (JDK, Maven, JExtract, solvers,
etc.) to create a consistent build environment.

* **Triggers**:
  * On a weekly schedule (`cron`).
  * On push to files within the `dev-env-fedora/` directory.
  * When a new GitHub Release is published.
* **Actions**:
  * Builds the Docker image from `dev-env-fedora/Dockerfile`.
  * Tags the image based on the trigger (e.g., `main`, release version like
    `v1.2.3`, `latest`).
  * Pushes the image to `ghcr.io`.
  * Signs the published image using `cosign` for supply-chain security.
  * Calls `verify-maven-build.yml` to confirm the project can be built with the
    new image.

### `publish-java-packages.yml`

This workflow is responsible for building and publishing the Java/Kotlin
packages to package registries.

* **Triggers**:
  * On push to the `main` branch.
  * Can be triggered manually (`workflow_dispatch`).
* **Actions**:
  * Runs inside the `dev-env-fedora` container to ensure a consistent build
    environment.
  * Sets up GPG keys for signing artifacts.
  * Uses a composite action (`publish-java-packages`) to run `mvn deploy`,
    publishing the packages to GitHub Packages and potentially Maven Central (
    for release/snapshot versions).
  * Calls `delete-old-packages.yml` to clean up old package versions after a
    successful publish.

### `release-java-packages.yml`

This workflow handles the formal release process, publishing the final,
non-snapshot Java/Kotlin packages to Maven Central. For the release process, we
first change the revision in the maven POM to be a non-snapshot version, commit
that, create a release and then post-release update the pom revision with the
`-SNAPSHOT` suffix again.

* **Triggers**:
  * When a new GitHub Release is published.
  * Can be triggered manually (`workflow_dispatch`).
* **Actions**:
  * Runs inside the `dev-env-fedora` container.
  * Explicitly sets the `isRelease: 'true'` flag when calling the
    `publish-java-packages` composite action, ensuring non-SNAPSHOT versions are
    published to Maven Central.

### `osv-scanner-scheduled-scan.yml`

This workflow enhances project security by scanning for vulnerable dependencies
using Google's OSV-Scanner.

* **Triggers**:
  * On push to the `main` branch.
  * On a weekly schedule (`cron`).
* **Actions**:
  * Runs the OSV scanner to analyze all project dependencies.
  * Uploads the results as a SARIF report, which populates the "Security" > "
    Code scanning alerts" tab in the repository with any findings.

### `delete-old-packages.yml`

This is a reusable workflow that cleans up old versions of the project's Maven
packages from GitHub Packages.

* **Triggers**:
  * Called by other workflows (`workflow_call`), specifically after a successful
    publish.
  * Can be triggered manually (`workflow_dispatch`).
* **Actions**:
  * Dynamically discovers all Maven modules in the project.
  * For each module, it deletes all but the two most recent package versions,
    helping to manage storage and keep the package registry tidy.

### `delete-old-docker-packages.yml`

This is a maintenance workflow designed to keep the GitHub Container Registry
clean.

* **Triggers**:
  * On a weekly schedule (`cron`).
  * Can be triggered manually (`workflow_dispatch`).
* **Actions**:
  * Deletes untagged versions of the `dev-env-fedora` Docker image that are
    older than one month. This helps to free up storage space by removing old,
    unused image layers.

