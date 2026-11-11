# Jenkins integration and deployment guide

This document explains how to build, test, containerize, publish, and deploy
the `student-management` application with Jenkins.

## 1. Project overview

The project is a Spring Boot REST API for managing students, departments,
courses, and enrollments.

| Area | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.5 |
| Build | Apache Maven 3.9.11 through Maven Wrapper |
| Web/API | Spring Boot Web MVC |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL |
| Code generation | Lombok 1.18.38 |
| API documentation | Springdoc OpenAPI 2.8.9 |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Coverage | JaCoCo 0.8.13 |
| Packaging | Executable Spring Boot JAR |
| Containerization | Docker, multi-stage Java 17 image |
| CI/CD | Jenkins Declarative Pipeline |

The application listens on port `8089` and uses the `/student` servlet context
path. For example, the application base URL is:

```text
http://localhost:8089/student
```

The OpenAPI UI is available at:

```text
http://localhost:8089/student/swagger-ui/index.html
```

## 2. Repository structure

```text
.
├── .dockerignore
├── .gitignore
├── .mvn/wrapper/
├── Dockerfile
├── Jenkinsfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java/tn/esprit/studentmanagement
    │   │   ├── controllers/       REST endpoints
    │   │   ├── dto/               Request DTOs
    │   │   ├── entities/          JPA entities
    │   │   ├── repositories/      Spring Data repositories
    │   │   ├── services/          Service interfaces and implementations
    │   │   └── StudentManagementApplication.java
    │   └── resources/
    │       └── application.properties
    └── test/java/                 Unit and Spring integration tests
```

Generated files under `target/` are build outputs. They are ignored by Git and
must not be used as the source for a Jenkins build.

## 3. Application configuration

The default configuration in `src/main/resources/application.properties` is:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/studentdb?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
server.port=8089
server.servlet.context-path=/student
```

Spring Boot environment variables override these values. The deployment stage
uses:

| Environment variable | Purpose |
|---|---|
| `SERVER_PORT` | Application port inside the container |
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password |

For production, provide database credentials through Jenkins credentials or a
secret-management system. Do not commit passwords to the repository.

## 4. Docker image

The `Dockerfile` uses two stages:

1. `maven:3.9.11-eclipse-temurin-17` downloads dependencies and creates the
   executable JAR with `./mvnw -B clean package -DskipTests`.
2. `eclipse-temurin:17-jre` runs only the packaged JAR as the non-root `spring`
   user.

The image exposes port `8089` and starts with:

```text
java -jar /app/app.jar
```

Build and run it locally:

```bash
docker build -t student-management:local .
docker run --rm --name student-management \
  -p 8089:8089 \
  -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/studentdb?createDatabaseIfNotExist=true' \
  -e SPRING_DATASOURCE_USERNAME='root' \
  -e SPRING_DATASOURCE_PASSWORD='' \
  --add-host host.docker.internal:host-gateway \
  student-management:local
```

The `.dockerignore` excludes Git metadata, IDE files, `target/`, and CI
configuration from the Docker build context.

## 5. Jenkins prerequisites

Install or provide the following on the Jenkins agent that runs this pipeline:

- Jenkins with Pipeline support.
- A Linux agent labeled `docker`.
- Java 17, if Maven commands are executed directly on the agent.
- Docker CLI and access to a running Docker daemon.
- Permission for the Jenkins user to execute Docker commands.
- Network access to Maven Central and the MySQL Docker image registry.
- Git checkout access to the repository.

The pipeline uses `sh`, so the `docker` agent must be a Unix/Linux agent. If
the agent is a Docker container, mount the Docker socket or use a remote Docker
daemon according to the organization's security policy.

The test stage starts MySQL on host port `3306`. Ensure that port is free on the
agent and that concurrent jobs are not sharing the same Docker host. The
pipeline also disables concurrent executions of this job.

## 6. Jenkins job creation

### Option A: Multibranch Pipeline

1. In Jenkins, select **New Item**.
2. Select **Multibranch Pipeline**.
3. Configure the Git repository source and its credentials.
4. Set branch discovery according to the team's workflow.
5. Ensure the repository contains `Jenkinsfile` at its root.
6. Save and run **Scan Multibranch Pipeline Now**.

### Option B: Pipeline job

1. Select **New Item** and create a **Pipeline** job.
2. Under **Pipeline**, select **Pipeline script from SCM**.
3. Select **Git**, enter the repository URL and branch.
4. Set the script path to `Jenkinsfile`.
5. Save the job and select **Build with Parameters**.

## 7. Jenkins credentials and registry setup

### Source repository credential

If the Git repository is private, create a Jenkins credential suitable for the
repository (SSH private key or username/password/token) and select it in the
SCM configuration.

### Docker registry credential

To publish the image to a private registry:

1. Go to **Manage Jenkins > Credentials**.
2. Create a **Username with password** credential.
3. Record its ID, for example `docker-registry`.
4. Use this ID as the `DOCKER_CREDENTIALS_ID` build parameter.
5. Set `DOCKER_REGISTRY` to the registry host, such as
   `registry.example.com`.

The pipeline logs in with `docker login --password-stdin`, tags the image with
the Jenkins build number, and pushes:

```text
registry.example.com/student-management:<BUILD_NUMBER>
```

An empty `DOCKER_REGISTRY` skips the publish stage.

## 8. Pipeline stages

The repository `Jenkinsfile` executes these stages:

### Checkout

Checks out the revision selected by Jenkins. The pipeline disables the default
checkout so that this explicit checkout is the only one performed.

### Start test database

Removes any stale test container named
`student-management-mysql-test`, then starts MySQL 8.4 with:

- Database: `studentdb`
- User: `root`
- Password: an internal CI-only password
- Host port: `3306`

The pipeline waits for `mysqladmin ping` to succeed before running tests.

### Test

Runs:

```bash
./mvnw -B clean verify
```

The JDBC settings are injected for the test process so the Spring integration
tests connect to the temporary MySQL container. Maven also generates the
JaCoCo report during the `verify` phase.

Jenkins publishes:

- Surefire XML reports from `target/surefire-reports/*.xml`
- JaCoCo output from `target/site/jacoco/**`

### Build image

Builds the image using the local repository and Jenkins build number:

```text
student-management:<BUILD_NUMBER>
```

### Publish image

Runs only when `DOCKER_REGISTRY` is non-empty. It tags and pushes the image to
the configured registry. Credentials are optional for public registries but
should be supplied for private registries.

### Deploy

Runs only when the `DEPLOY` parameter is enabled. It removes an existing
container with the configured name and starts the locally built image with:

- `--restart unless-stopped`
- Host port `${DEPLOY_PORT}` mapped to container port `8089`
- Database environment variables supplied by build parameters
- `host.docker.internal` mapped to the Docker host gateway

The deployment target is the Docker host of the Jenkins agent. This is a simple
single-host deployment, not a Kubernetes or multi-node deployment.

### Post actions

Regardless of success or failure, the test MySQL container is removed and the
packaged JAR is archived when available. On success, Jenkins prints the image
name and tag.

## 9. Build parameters

| Parameter | Default | Description |
|---|---|---|
| `DEPLOY` | `false` | Start or replace the deployment container |
| `IMAGE_REPOSITORY` | `student-management` | Local image/repository name |
| `DOCKER_REGISTRY` | empty | Registry host; empty skips publishing |
| `DOCKER_CREDENTIALS_ID` | empty | Jenkins credential ID for registry login |
| `DEPLOY_CONTAINER` | `student-management` | Deployment container name |
| `DEPLOY_PORT` | `8089` | Host port exposed by the deployed container |
| `DATABASE_URL` | `jdbc:mysql://host.docker.internal:3306/studentdb?...` | Production JDBC URL |
| `DATABASE_USERNAME` | `root` | Production database username |
| `DATABASE_PASSWORD` | empty | Production database password |

Recommended build flows:

### Validation only

Use the defaults. This runs checkout, MySQL-backed tests, JaCoCo, and Docker
image creation without publishing or deploying.

### Publish an image

Set:

```text
DOCKER_REGISTRY=registry.example.com
DOCKER_CREDENTIALS_ID=docker-registry
```

Leave `DEPLOY` disabled unless the Jenkins host is also the deployment host.

### Deploy on the Jenkins Docker host

Set `DEPLOY=true` and provide a reachable MySQL instance through
`DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.

## 10. Manual verification after deployment

From a machine that can reach the Jenkins Docker host:

```bash
curl -i http://<host>:<DEPLOY_PORT>/student/swagger-ui/index.html
```

Check the container and application logs on the Jenkins agent:

```bash
docker ps
docker logs --tail=200 student-management
```

Check the database connection from the logs if the application fails during
startup. The application uses `spring.jpa.hibernate.ddl-auto=update`, so the
configured database user must have permission to create or alter the required
tables.

## 11. Local commands that mirror CI

Compile and package without tests:

```bash
./mvnw -B -DskipTests package
```

Run all tests against a local MySQL instance:

```bash
SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/studentdb?createDatabaseIfNotExist=true' \
SPRING_DATASOURCE_USERNAME='root' \
SPRING_DATASOURCE_PASSWORD='<password>' \
./mvnw -B clean verify
```

Run the database-independent controller unit tests:

```bash
./mvnw -B -Dtest=ControllerUnitTest test
```

On Windows, use `mvnw.cmd` and set environment variables with PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3306/studentdb?createDatabaseIfNotExist=true"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "<password>"
.\mvnw.cmd -B clean verify
```

## 12. Troubleshooting

### `docker: permission denied`

Add the Jenkins service account to the Docker group or configure the agent to
use an authorized remote Docker daemon. Restart the agent after changing group
membership.

### `Cannot connect to the Docker daemon`

Start Docker on the agent and verify:

```bash
docker version
```

### MySQL readiness timeout

Check whether port `3306` is already occupied, then inspect:

```bash
docker logs student-management-mysql-test
```

Remove a stale container if necessary:

```bash
docker rm -f student-management-mysql-test
```

### Spring tests fail with `Communications link failure`

The test database is unavailable or the JDBC variables were not applied.
Confirm the MySQL stage completed successfully and that the agent can connect
to `127.0.0.1:3306`.

### Image push is denied

Verify the registry hostname, image permissions, credential ID, and repository
path. For a private registry, ensure the credential is a Jenkins
username/password credential and that the account can push the image.

### Deployment starts but the API is unavailable

Check:

1. The host port is free.
2. `docker logs student-management` contains no database startup error.
3. `DATABASE_URL` points to a database reachable from the Docker host.
4. The firewall allows the selected `DEPLOY_PORT`.
5. Requests include the `/student` context path.

## 13. Production recommendations

Before using this pipeline for production, consider:

- Store database passwords and registry credentials only in Jenkins Credentials
  or an external secret manager.
- Use a dedicated deployment agent instead of deploying from a general build
  agent.
- Use immutable image tags such as Git commit SHA in addition to build numbers.
- Add an approval gate before the `Deploy` stage.
- Use a managed MySQL instance and a least-privilege database account.
- Replace `spring.jpa.hibernate.ddl-auto=update` with controlled migrations
  such as Flyway or Liquibase.
- Add a health endpoint and a post-deployment smoke test.
- Configure HTTPS, CORS, authentication, backups, monitoring, and log
  retention according to the target environment.
