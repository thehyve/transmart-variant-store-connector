# Running variant store connector in a container

## Container tools

This project relies on [Container Tools](https://github.com/containers) project.
Check its page for details.

[Buildah](https://buildah.io/) and [Podman](https://podman.io/) are used for building and running a container for Variant store connector.

Fedora and RHEL (CentOS) distros have these tools in standard repositories.

For installing them in Ubuntu distro check [Project Atomic PPA](https://launchpad.net/~projectatomic/+archive/ubuntu/ppa).

After installation check if you have `/etc/containers/registries.conf` file with some content, similar to:
```
[registries.search]
registries = ['docker.io', 'registry.fedoraproject.org', 'quay.io', 'registry.access.redhat.com', 'registry.centos.org']
```

## Baking your own image

To build your own container image run following command:
```bash
buildah unshare ./buildah.sh
```

To copy the image to the local Docker repository, run:
```bash
skopeo copy containers-storage:docker.io/thehyve/transmart-variant-store-connector:latest docker-daemon:thehyve/transmart-variant-store-connector:latest
```

## Getting prebuilded images from The Hyve

We have already prebuilded images at Docker Hub.
You can get one by runnung one of following commands:
* Podman
    ```bash
    podman pull docker.io/thehyve/transmart-variant-store-connector:latest
    podman pull docker.io/thehyve/transmart-variant-store-connector:0.0.1
    ```
* Docker
    ```bash
    docker pull thehyve/transmart-variant-store-connector:latest
    docker pull thehyve/transmart-variant-store-connector:0.0.1
    ```

## Running a container

To run an instance of variant store container run one of following commands:
* Podman
    ```bash
    podman run -p 9060:9060 -it --rm --env KEYCLOAK_SERVER_URL=https://keycloak.example.com/auth --env KEYCLOAK_REALM=transmart --env KEYCLOAK_CLIENT_ID=transmart-client --env TRANSMART_API_SERVER_URL=https://transmart-dev.thehyve.net --env VARIANT_STORE_URL=https://variant-store.example.com docker.io/thehyve/transmart-variant-store-connector:latest
    podman run -p 9060:9060 -it --rm --env KEYCLOAK_SERVER_URL=https://keycloak.example.com/auth --env KEYCLOAK_REALM=transmart --env KEYCLOAK_CLIENT_ID=transmart-client --env TRANSMART_API_SERVER_URL=https://transmart-dev.thehyve.net --env VARIANT_STORE_URL=https://variant-store.example.com docker.io/thehyve/transmart-variant-store-connector:0.0.1
    ```
* Docker
    ```bash
    docker run -p 9060:9060 -it --rm --env KEYCLOAK_SERVER_URL=https://keycloak.example.com/auth --env KEYCLOAK_REALM=transmart --env KEYCLOAK_CLIENT_ID=transmart-client --env TRANSMART_API_SERVER_URL=https://transmart-dev.thehyve.net --env VARIANT_STORE_URL=https://variant-store.example.com thehyve/transmart-variant-store-connector:latest
    docker run -p 9060:9060 -it --rm --env KEYCLOAK_SERVER_URL=https://keycloak.example.com/auth --env KEYCLOAK_REALM=transmart --env KEYCLOAK_CLIENT_ID=transmart-client --env TRANSMART_API_SERVER_URL=https://transmart-dev.thehyve.net --env VARIANT_STORE_URL=https://variant-store.example.com thehyve/transmart-variant-store-connector:0.0.1
    ```
