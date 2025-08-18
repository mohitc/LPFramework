# Setting up GPG keys for this Action

Instructions for setting up GPG keys for publishing are outlined at various
sites
including [Maven Central](https://central.sonatype.org/publish/requirements/gpg/).
Once a key-pair has been generated, you need to include that as a secret to be
used in Github workflows.

This workflow assumes that the key comes as a `base64` encoded version of the
key. In order to prepare the GPG key for export, run the following commands:

```
gpg --armor --export-secret-keys {key-id} | base64  | tr -d  '\n'
```