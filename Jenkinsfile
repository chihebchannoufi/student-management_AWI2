pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    environment {
        // SonarQube and Nexus
        SONAR_URL = 'http://sonarqube:9000'
        NEXUS_URL = 'http://nexus:8081/repository/maven-snapshots/'

        // Docker configuration
        IMAGE_NAME = 'student-management'
        IMAGE_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'student-management-app'

        // Application ports
        APP_PORT = '8085'
        CONTAINER_PORT = '8089'

        // WAMP database configuration
        DB_NAME = 'studentdb'
        DB_HOST = 'host.docker.internal'
        DB_PORT = '3306'
    }

    stages {

        // =====================================
        // 1. CHECKOUT GIT
        // =====================================
        stage('CHECKOUT GIT') {
            steps {
                checkout scm

                sh '''
                    echo "===== GIT CHECKOUT ====="
                    git branch --show-current
                    git log -1 --oneline
                '''
            }
        }

        // =====================================
        // 2. MAVEN CLEAN
        // =====================================
        stage('MVN CLEAN') {
            steps {
                sh '''
                    echo "===== MAVEN CLEAN ====="
                    chmod +x mvnw
                    ./mvnw clean
                '''
            }
        }

        // =====================================
        // 3. COMPILE PROJECT
        // =====================================
        stage('ARTIFACT CONSTRUCTION') {
            steps {
                sh '''
                    echo "===== MAVEN COMPILE ====="
                    ./mvnw compile
                '''
            }
        }

        // =====================================
        // 4. UNIT TESTS
        // =====================================
        stage('UNIT TESTS') {
            steps {
                sh '''
                    echo "===== UNIT TESTS ====="
                    ./mvnw test -Dtest=DepartmentServiceMockTest
                '''
            }
        }

        // =====================================
        // 5. SONARQUBE ANALYSIS
        // =====================================
        stage('MVN SONARQUBE') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonar-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh '''
                        echo "===== SONARQUBE ANALYSIS ====="

                        ./mvnw jacoco:report sonar:sonar \
                            -Dsonar.projectKey=student-management \
                            -Dsonar.host.url=${SONAR_URL} \
                            -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }

        // =====================================
        // 6. PUBLISH JAR TO NEXUS
        // =====================================
        stage('PUBLISH TO NEXUS') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {
                    sh '''
                        set -e

                        echo "===== PUBLISH TO NEXUS ====="

                        cat > nexus-settings.xml <<EOF
<settings>
    <servers>
        <server>
            <id>nexus-snapshots</id>
            <username>${NEXUS_USER}</username>
            <password>${NEXUS_PASS}</password>
        </server>
    </servers>
</settings>
EOF

                        ./mvnw deploy \
                            -s nexus-settings.xml \
                            -DskipTests \
                            -DaltDeploymentRepository=nexus-snapshots::${NEXUS_URL}

                        rm -f nexus-settings.xml
                    '''
                }
            }
        }

        // =====================================
        // 7.BUILDING OUR IMAGE
        // =====================================
        stage('BUILDING OUR IMAGE') {
            steps {
                sh '''
                    set -e

                    echo "===== BUILD DOCKER IMAGE ====="

                    docker build \
                        -t ${IMAGE_NAME}:1.0 \
                        -t ${IMAGE_NAME}:${IMAGE_TAG} \
                        -f Dockerfile .

                    echo "===== AVAILABLE IMAGES ====="
                    docker images ${IMAGE_NAME}
                '''
            }
        }

        // =====================================
        // 8. DEPLOY OUR IMAGE (Docker Hub Push)
        // =====================================
        stage('DEPLOY OUR IMAGE') {
    steps {
        sh '''
            set -e

            echo "===== REMOVE OLD CONTAINER ====="

            docker rm -f ${CONTAINER_NAME} || true

            echo "===== DEPLOY NEW CONTAINER ====="

            docker run -d \
                --name ${CONTAINER_NAME} \
                --restart unless-stopped \
                -p ${APP_PORT}:${CONTAINER_PORT} \
                -e SPRING_DATASOURCE_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabaseIfNotExist=true" \
                -e SPRING_DATASOURCE_USERNAME=root \
                -e SPRING_DATASOURCE_PASSWORD= \
                ${IMAGE_NAME}:${IMAGE_TAG}

            echo "===== VERIFY CONTAINER ====="

            docker ps --filter name=${CONTAINER_NAME}

            echo "SUCCESS: Docker container deployed."
        '''
    }
}
        // =====================================
        // 9. Test AWS Credentials
        // =====================================
        stage('Test AWS Credentials') {
    steps {
        withCredentials([[
            $class: 'AmazonWebServicesCredentialsBinding',
            credentialsId: 'aws-credentials',
            accessKeyVariable: 'AWS_ACCESS_KEY_ID',
            secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
        ]]) {
            sh '''
                set -e

                echo "===== CHECK AWS CLI ====="
                aws --version

                echo "===== VERIFY AWS CREDENTIALS ====="
                aws sts get-caller-identity --output json

                echo "SUCCESS: Jenkins connected to AWS."
            '''
        }
    }
}
        // =====================================
        // 10. SETUP TERRAFORM
        // =====================================
        stage('SETUP TERRAFORM') {
            steps {
                sh '''
                    set -e

                    echo "===== DEPLOY NEW CONTAINER ====="

                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        -p ${APP_PORT}:${CONTAINER_PORT} \
                        -e SPRING_DATASOURCE_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabaseIfNotExist=true" \
                        -e SPRING_DATASOURCE_USERNAME=root \
                        -e SPRING_DATASOURCE_PASSWORD= \
                        ${IMAGE_NAME}:${IMAGE_TAG}

                    echo "===== WAIT FOR APPLICATION ====="

                    STARTED=false

                    for i in $(seq 1 30); do

                        if docker logs ${CONTAINER_NAME} 2>&1 | grep -q "Started"; then
                            STARTED=true
                            break
                        fi

                        sleep 5
                    done

                    if [ "$STARTED" != "true" ]; then
                        echo "ERROR: Application failed to start."
                        docker logs ${CONTAINER_NAME}
                        exit 1
                    fi

                    echo "SUCCESS: Application started."
                '''
            }
        }

        // =====================================
        // 11. UPDATE KUBECONFIG
        // =====================================
        stage('UPDATE KUBECONFIG') {
            steps {
                sh '''
                    set -e

                    echo "===== CONTAINER VERIFICATION ====="

                    docker ps --filter name=${CONTAINER_NAME}

                    RUNNING=$(docker inspect \
                        --format='{{.State.Running}}' \
                        ${CONTAINER_NAME})

                    if [ "$RUNNING" != "true" ]; then
                        echo "ERROR: Container is not running."
                        docker logs ${CONTAINER_NAME}
                        exit 1
                    fi

                    echo "SUCCESS: Container is running."
                '''
            }
        }

        // =====================================
        // 12. DEPLOY TO AWS KUBERNETES
        // =====================================
        stage('DEPLOY TO AWS KUBERNETES') {
            steps {
                sh '''
                    set -e

                    echo "===== SWAGGER API DOCS TEST ====="

                    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
                        --connect-timeout 5 \
                        --max-time 30 \
                        http://host.docker.internal:${APP_PORT}/student/v3/api-docs)

                    echo "Swagger HTTP status: $HTTP_CODE"

                    if [ "$HTTP_CODE" != "200" ]; then
                        echo "ERROR: Swagger verification failed."
                        docker logs ${CONTAINER_NAME}
                        exit 1
                    fi

                    echo "SUCCESS: Swagger is responding."

                    echo "===== DEPARTMENT API TEST ====="

                    API_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
                        --connect-timeout 5 \
                        --max-time 30 \
                        http://host.docker.internal:${APP_PORT}/student/Department/getAllDepartment)

                    echo "Department API HTTP status: $API_CODE"

                    if [ "$API_CODE" != "200" ]; then
                        echo "ERROR: Department API verification failed."
                        exit 1
                    fi

                    echo "SUCCESS: Department API is responding."
                '''
            }
        }
    }
}