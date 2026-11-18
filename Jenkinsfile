
pipeline {
    agent any

    environment {
        SONAR_URL = 'http://sonarqube:9000'
        NEXUS_URL = 'http://nexus:8081/repository/maven-snapshots/'

        DOCKER_IMAGE = 'student-management:1.0'
        CONTAINER_NAME = 'student-management-app'

        APP_PORT = '8085'
        CONTAINER_PORT = '8089'
    }

    stages {

        stage('CHECKOUT GIT') {
            steps {
                echo 'Recuperation du projet depuis GitHub'
                checkout scm
            }
        }

        stage('MVN CLEAN') {
            steps {
                echo 'Nettoyage du projet'
                sh '''
                    chmod +x mvnw
                    ./mvnw clean
                '''
            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps {
                echo 'Compilation du projet'
                sh './mvnw compile'
            }
        }

        stage('UNIT TESTS') {
            steps {
                echo 'Execution des tests unitaires'
                sh './mvnw test -Dtest=DepartmentServiceMockTest'
            }
        }

        stage('MVN SONARQUBE') {
            steps {
                echo 'Analyse de la qualite du code'

                withCredentials([
                    string(
                        credentialsId: 'sonar-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh '''
                        ./mvnw jacoco:report sonar:sonar \
                        -Dsonar.projectKey=student-management \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('PUBLISH TO NEXUS') {
            steps {
                echo 'Publication du JAR dans Nexus'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {
                    sh '''
                        set -e

                        printf "<settings><servers><server><id>nexus-snapshots</id><username>%s</username><password>%s</password></server></servers></settings>" \
                        "$NEXUS_USER" "$NEXUS_PASS" > nexus-settings.xml

                        ./mvnw deploy -s nexus-settings.xml -DskipTests \
                        -DaltDeploymentRepository=nexus-snapshots::default::${NEXUS_URL}

                        rm -f nexus-settings.xml
                    '''
                }
            }
        }

        stage('BUILDING OUR IMAGE') {
            steps {
                echo 'Construction de la vraie image Docker'

                sh '''
                    docker build \
                    -t ${DOCKER_IMAGE} \
                    -t student-management:${BUILD_NUMBER} \
                    -f Dockerfile .
                '''

                echo 'Docker image construite avec succes'
            }
        }

        stage('DEPLOY OUR IMAGE') {
            steps {
                echo 'Deploiement de Student Management'

                sh '''
                    set -e

                    # Supprimer uniquement l'ancien container applicatif
                    docker rm -f ${CONTAINER_NAME} || true

                    # Demarrer la nouvelle version
                    docker run -d \
                    --name ${CONTAINER_NAME} \
                    --restart unless-stopped \
                    -p ${APP_PORT}:${CONTAINER_PORT} \
                    -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/studentdb?createDatabaseIfNotExist=true' \
                    -e SPRING_DATASOURCE_USERNAME=root \
                    -e SPRING_DATASOURCE_PASSWORD='' \
                    ${DOCKER_IMAGE}

                    echo "Attente du demarrage de Spring Boot..."

                    STARTED=false

                    for i in $(seq 1 30); do

                        if docker logs ${CONTAINER_NAME} 2>&1 | grep -q 'Started '; then
                            STARTED=true
                            break
                        fi

                        RUNNING=$(docker inspect -f '{{.State.Running}}' ${CONTAINER_NAME} 2>/dev/null || echo false)

                        if [ "$RUNNING" != "true" ]; then
                            echo "ERREUR: Le container est arrete"
                            docker logs ${CONTAINER_NAME}
                            exit 1
                        fi

                        sleep 5
                    done

                    if [ "$STARTED" != "true" ]; then
                        echo "ERREUR: Spring Boot n'a pas confirme son demarrage"
                        docker logs ${CONTAINER_NAME}
                        exit 1
                    fi

                    echo "Application demarree avec succes"
                    docker ps --filter name=${CONTAINER_NAME}
                '''
            }
        }

        stage('VERIFY DEPLOYMENT') {
            steps {
                echo 'Verification du container deploye'

                sh '''
                    docker inspect \
                    --format='Container running: {{.State.Running}}' \
                    ${CONTAINER_NAME}

                    docker logs --tail 30 ${CONTAINER_NAME}
                '''
            }
        }

        // AWS, Terraform et Kubernetes restent non configures.
        // On ne les declare pas SUCCESS artificiellement.
    }

    post {
        success {
            echo 'PIPELINE SUCCESS: Build et Deploy Docker termines'
        }

        failure {
            echo 'PIPELINE FAILED: Consulter les logs de la stage en erreur'
        }

        always {
            echo 'Fin du pipeline Jenkins'
        }
    }
}
