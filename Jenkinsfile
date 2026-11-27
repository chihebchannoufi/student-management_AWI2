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
        // 7. BUILDING OUR IMAGE
        // =====================================
        stage('BUILDING OUR IMAGE') {
            steps {
                sh '''
                    set -e
                    echo "===== BUILD DOCKER IMAGE ====="
                    docker build \
                        -t chdouda/${IMAGE_NAME}:1.0 \
                        -t chdouda/${IMAGE_NAME}:${IMAGE_TAG} \
                        -f Dockerfile .
                '''
            }
        }

        // =====================================
        // 8. DEPLOY OUR IMAGE (Docker Hub Push)
        // =====================================
        stage('DEPLOY OUR IMAGE') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        set -e
                        echo "===== PUSH IMAGE TO DOCKER HUB ====="
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push chdouda/${IMAGE_NAME}:1.0
                        docker push chdouda/${IMAGE_NAME}:${IMAGE_TAG}
                        docker logout
                        echo "SUCCESS: Image pushed to Docker Hub."
                    '''
                }
            }
        }

        // =====================================
        // 9. TEST AWS CREDENTIALS
        // =====================================
        stage('TEST AWS CREDENTIALS') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    sh '''
                        set -eu
                        set +x
                        echo "===== CHECK AWS CLI ====="
                        aws --version
                        echo "===== VERIFY AWS CONNECTION ====="
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
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    sh '''
                        set -eu
                        set +x
                        cd terraform
                        echo "===== TERRAFORM INIT ====="
                        terraform init
                        echo "===== TERRAFORM VALIDATE ====="
                        terraform validate
                        echo "===== TERRAFORM APPLY ====="
                        terraform apply -auto-approve
                        echo "SUCCESS: EKS Cluster ready."
                    '''
                }
            }
        }

        // =====================================
        // 11. UPDATE KUBECONFIG
        // =====================================
        stage('UPDATE KUBECONFIG') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    sh '''
                        set -eu
                        set +x
                        echo "===== UPDATE KUBECONFIG ====="
                        aws eks update-kubeconfig --region us-east-1 --name student-management-eks
                        kubectl get nodes
                        echo "SUCCESS: Kubeconfig updated."
                    '''
                }
            }
        }

        // =====================================
        // 12. DEPLOY TO AWS KUBERNETES
        // =====================================
        stage('DEPLOY TO AWS KUBERNETES') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    sh '''
                        set -eu
                        set +x
                        echo "===== DEPLOY TO EKS ====="
                        kubectl apply -f k8s/deploy.yaml
                        kubectl rollout status deployment/mysql --timeout=180s
                        kubectl rollout status deployment/student-management --timeout=180s

                        echo "===== GET LOADBALANCER URL ====="
                        kubectl get pods
                        kubectl get svc student-management-service
                        echo "SUCCESS: Deployed to AWS Kubernetes."
                    '''
                }
            }
        }
    }

    // =====================================
    // EMAIL NOTIFICATIONS
    // =====================================
    post {
        success {
            mail to: 'chnada780@gmail.com',
                 subject: "SUCCESS: Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "le build #${env.BUILD_NUMBER} et success.: ${env.BUILD_URL}console"
        }
        failure {
            mail to: 'chnada780@gmail.com',
                 subject: "FAILURE: Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "le build #${env.BUILD_NUMBER} failed.l'erreur est : ${env.BUILD_URL}console"
        }
    }
}