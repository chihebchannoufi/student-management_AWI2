def withAws(Closure body) {
    withCredentials([
        string(credentialsId: 'aws-access-key-id',     variable: 'AWS_ACCESS_KEY_ID'),
        string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
        string(credentialsId: 'aws-session-token',     variable: 'AWS_SESSION_TOKEN')
    ]) {
        withEnv(['AWS_DEFAULT_REGION=us-east-1']) {
            body()
        }
    }
}

pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    parameters {
        booleanParam(name: 'RUN_AWS', defaultValue: false, description: 'Run the AWS / Terraform / Kubernetes stages')
    }

    environment {
        IMAGE_NAME = 'youssefbenkadria/student-management'
        SONAR_URL  = 'http://localhost:9000'
    }

    stages {
        stage('CHECKOUT GIT') {
            steps { checkout scm }
        }

        stage('MVN CLEAN') {
            steps { bat 'mvn clean' }
        }

        stage('UNIT TESTS') {
            steps { bat 'mvn test' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps { bat 'mvn package -DskipTests' }
        }

        stage('MVN SONARQUBE') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389:sonar -Dsonar.projectKey=student-management -Dsonar.projectName=student-management -Dsonar.host.url=%SONAR_URL% -Dsonar.token=%SONAR_TOKEN%'
                }
            }
        }

        stage('PUBLISH TO NEXUS') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    bat 'mvn deploy -DskipTests -s settings.xml'
                }
            }
        }

        stage('BUILDING OUR IMAGE') {
            steps {
                bat 'docker build -t %IMAGE_NAME%:%BUILD_NUMBER% -t %IMAGE_NAME%:latest .'
            }
        }

        stage('PUSH TO DOCKERHUB') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    bat 'echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin'
                    bat 'docker push %IMAGE_NAME%:%BUILD_NUMBER%'
                    bat 'docker push %IMAGE_NAME%:latest'
                }
            }
        }

        stage('Test AWS Credentials') {
            when { expression { params.RUN_AWS } }
            steps {
                withAws { bat 'aws sts get-caller-identity' }
            }
        }

        stage('SETUP TERRAFORM') {
            when { expression { params.RUN_AWS } }
            steps {
                withAws {
                    dir('terraform') {
                        bat 'terraform init'
                        bat 'terraform apply -auto-approve'
                    }
                }
            }
        }

        stage('UPDATE KUBECONFIG') {
            when { expression { params.RUN_AWS } }
            steps {
                withAws { bat 'aws eks update-kubeconfig --region us-east-1 --name YOUR_CLUSTER_NAME' }
            }
        }

        stage('DEPLOY TO AWS KUBERNETES') {
            when { expression { params.RUN_AWS } }
            steps {
                withAws {
                    bat 'kubectl apply -f k8s/'
                    bat 'kubectl rollout restart deployment/student-management'
                    bat 'kubectl rollout status deployment/student-management --timeout=180s'
                }
            }
        }
    }

    post {
        success { echo 'Pipeline finished successfully.' }
        failure { echo 'Pipeline failed, check the console output.' }
    }
}