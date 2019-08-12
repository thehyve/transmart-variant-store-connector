[![Build status](https://travis-ci.org/thehyve/transmart-variant-store-connector.svg?branch=master)](https://travis-ci.org/thehyve/transmart-variant-store-connector/branches)
[![codecov](https://codecov.io/gh/thehyve/transmart-variant-store-connector/branch/master/graph/badge.svg)](https://codecov.io/gh/thehyve/transmart-variant-store-connector)
[![codebeat](https://codebeat.co/badges/ceb304f9-7efc-4ba5-b9cb-45959222f780)](https://codebeat.co/a/gijs-kant/projects/github-com-thehyve-transmart-variant-store-connector-master)

# Variant store connector for TranSMART

Spring Boot application that serves as a TranSMART REST API proxy, using
a [Variant Store](https://github.com/qbicsoftware/oncostore-proto-project) to query for patients linked to variants. 
See below for instructions on running the proxy server.


#### Configure Keycloak and TranSMART connection

The transmart-proxy-server uses Keycloak as an identity provider.
The following settings need to be configured before running the application.

| Property                                 | Description
|:---------------------------------------- |:--------------------------------
| `keycloak.auth-server-url`               | Keycloak url, e.g, `https://keycloak.example.com/auth`
| `keycloak.realm`                         | Keycloak realm.
| `keycloak.resource`                      | Keycloak client id.
| `transmart-client.transmart-server-url`  | The TranSMART server url, e.g., `https://transmart-dev.thehyve.net`
| `variant-store-client.variant-store-url` | The variant store url, e.g., `https://variant-store.example.com`

See [application-dev.yml](src/main/resources/config/application-dev.yml)
for example configuration.

#### Run

Make sure you have Java 11 and Maven installed.
```bash
# create a jar package
mvn clean package
```
There should now be a `.jar`-file in `target/transmart-variant-store-connector-<version>.jar`.
```bash
# run the packaged application
java -jar -Dspring.config.location=classpath:config/application.yml,/path/to/config.yml target/transmart-variant-store-connector-<version>.jar
```

There should now be an application running at [http://localhost:9060/](http://localhost:9060/).



## Development

### Run in development mode

```bash
# run the application in development mode
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Install
```bash
# install the package locally
mvn clean install
```

### Tests

Run all tests:
```bash
mvn test
```



## License

Copyright (c) 2019 The Hyve B.V.

The Variant store connector for TranSMART is licensed under the MIT License.
See the file [LICENSE](LICENSE).

