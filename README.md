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

##### From a Nexus repository

Deployment artefacts are published to [the Nexus repository of The Hyve](https://repo.thehyve.nl/).

To fetch and run [transmart-variant-store-connector-0.0.2.jar](https://repo.thehyve.nl/service/local/repositories/releases/content/nl/thehyve/transmart-variant-store-connector/0.0.2/transmart-variant-store-connector-0.0.2.jar):
```bash
# Fetch artefact from Maven
curl -f -L https://repo.thehyve.nl/service/local/repositories/releases/content/nl/thehyve/transmart-variant-store-connector/0.0.2/transmart-variant-store-connector-0.0.2.jar -o transmart-variant-store-connector-0.0.2.jar && \
# Run it with:
java -jar -Dspring.config.location=classpath:config/application.yml,/path/to/config.yml target/transmart-variant-store-connector-0.0.2.jar
```

##### From sources
```bash
# create a jar package
mvn clean package
```
There should now be a `.jar`-file in `target/transmart-variant-store-connector-0.0.2.jar`.
```bash
# run the packaged application
java -jar -Dspring.config.location=classpath:config/application.yml,/path/to/config.yml target/transmart-variant-store-connector-0.0.2.jar
```

There should now be an application running at [http://localhost:9060/](http://localhost:9060/).


## Usage

The application serves a REST API, documentation is available as the default page of the application: [http://localhost:9060/](http://localhost:9060/).

### Authentication

Use the`Authorize` button to authenticate.
A token can be obtained from Keycloak:
```bash
curl "${KEYCLOAK_SERVER_URL}/auth/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
  -d "client_id=${KEYCLOAK_CLIENT_ID}" \
  -d "username=${KEYCLOAK_USERNAME}" \
  -d "password=${KEYCLOAK_PASSWORD}" \
  -d 'grant_type=password'
```
Use the value of the `access_token` field in the response as API key in the authorization dialog.
N.B.: the access token typically expires quite soon. 

You may consider to use the [TranSMART API client for Python](https://github.com/thehyve/transmart-api-client-py)
to interactively query the API. The API client uses offline token to obtain an access token automatically.
The client converts responses to Pandas data frames.

### Querying

Constraints are passed to the API server in JSON format.

To select all data in TranSMART:
```json
{"constraint": {"type": "true"}}
```

To obtain counts for a constraint in `example.json`:
```bash
curl -v -H "Authorization: Bearer ${ACCESS_TOKEN}" -H 'Content-Type: application/json' -d '@example.json' http://localhost:9060/v2/observations/counts
```

Example of a variant query that could be used in the `/v2/observations` endpoint of the variant store connector:
```json
{"type": "clinical", "constraint": {"type": "biomarker", "biomarkerType":"variant", "params": {"chromosome": "11"}}}
```
This selects patients for which there are variants in the database for chromosome 11 (in practice this selects all patients)

An example of a combination query:
```json
{"type": "clinical", "constraint": {"type": "and", "args": [
    {"type": "subselection", "dimension": "patient", "constraint": {"type": "biomarker", "biomarkerType":"variant", "params": {"chromosome": "11"}}},
    {"type": "subselection", "dimension": "patient", "constraint": {"type": "and", "args": [
        {"type": "concept", "conceptCode": "Patient.gender"},
        {"type": "value", "valueType": "string", "operator": "=", "value": "male"}
    ]}}
]}}
```
This selects patients with value `male` for the `Patient.gender` concept (all male subjects) for which
there is variant data for chromosome 11. 

Error handling in the variant store connector is currently very basic: any issue (e.g., concept does not exist or connection with variant store),
results in `500 Internal Server Error`.



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


## Acknowledgement

This project was funded by the German Ministry of Education and Research (BMBF) as part of the project
DIFUTURE - Data Integration for Future Medicine within the German Medical Informatics Initiative (grant no. 01ZZ1804D).


## License

Copyright (c) 2019 The Hyve B.V.

The Variant store connector for TranSMART is licensed under the MIT License.
See the file [LICENSE](LICENSE).
