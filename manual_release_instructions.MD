# Manual Release Instructions

As CPLEX jars are not published to a Maven repository, we need to perform a deployment from a workspace that has the
cplex JARs available locally. The instructions below outline the pre-requisites for setting up the local environment for
a deployment to maven central.

## Step 1: Install CPLEX JAR locally

Follow the instructions in
the [CPLEX Solver README](./cplex-solver/README.md?#option-2-using-the-cplexjar-from-your-local-installation)
to install the CPLEX jar locally as a library.

## Step 2: Configure settings.xml

Create a copy of the [settings.xml](./dev-env-fedora/settings.xml) file and configure the

* Maven central credentials
* GPG Passphrase associated with the GPG signing key

## Step 3: Run Docker images with local Maven Repository

Navigate to the root of the code directory in your local workspace and run the following command, replacing the
`home_directory` with the appropriate path to run the docker development environment with the local maven repository
mounted into the docker image. Make sure to use Docker rootless mode in order to not have directories created as the
root user in your local `.m2` repository in this step.

```shell
docker run \
  --mount type=bind,src=./,dst=/root/projects/LPFramework \
  --mount type=bind,src=${home_direcotry}/.m2,dst=/root/.m2 \
  -it ghcr.io/mohitc/lpframework/dev-env-fedora:main
```

## Step 4: Configure GPG key

Follow the [steps](./.github/actions/setup-gpg/README.md) to import the GPG key into to docker image.

## Step 5: Validate the compilation

Once installed test that the compilation and tests are working as expected using the command

```shell
mvn -P default,cplex clean install
```

## Step 6: Deploy

In order to deploy to maven central, run the command below:

```shell
mvn -P publish-central,default,cplex  -Dmaven.test.skip=true --batch-mode clean deployC
```

If the command completes successfully, you will see a deployment in the [Maven Central](https://central.sonatype.com/)
dashboard. From there, publish the package to complete the process. 
