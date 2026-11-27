def withAws(Closure body) {
    withCredentials([
        string(credentialsId: 'aws-access-key-id',     variable: 'AWS_ACCESS_KEY_ID'),
        string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
        string(credentialsId: 'aws-session-token',     variable: 'AWS_SESSION_TOKEN')
    ]) {
        withEnv(['AWS_DEFAULT_REGION=us-west-2']) {
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
        booleanParam(name: 'RUN_AWS', defaultValue: true, description: 'Run the AWS / Terraform / Kubernetes stages')
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
                        bat 'terraform init -input=false'
                        bat 'terraform apply -auto-approve -input=false'
                    }
                }
            }
        }

        stage('UPDATE KUBECONFIG') {
            when { expression { params.RUN_AWS } }
            steps {
                withAws { bat 'aws eks update-kubeconfig --region us-west-2 --name student-cluster' }
            }
        }

        stage('DEPLOY TO AWS KUBERNETES') {
            when { expression { params.RUN_AWS } }
            steps {
                withAws {
                    bat 'kubectl apply -f k8s/'
                    bat 'kubectl rollout restart deployment/student-management'
                    bat 'kubectl rollout status deployment/student-management --timeout=300s'
                }
            }
        }
    }

    post {
        success {
            script {
                try {
                    emailext(
                        to: 'ybenkadria@gmail.com',
                        subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: "The pipeline ${env.JOB_NAME} build #${env.BUILD_NUMBER} finished successfully.\n\nDetails: ${env.BUILD_URL}"
                    )
                } catch (e) {
                    echo "Email could not be sent: ${e.message}"
                }
            }
        }
        failure {
            script {
                try {
                    emailext(
                        to: 'ybenkadria@gmail.com',
                        subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: "The pipeline ${env.JOB_NAME} build #${env.BUILD_NUMBER} FAILED.\n\nConsole: ${env.BUILD_URL}console\n\nThe full log is attached.",
                        attachLog: true
                    )
                } catch (e) {
                    echo "Email could not be sent: ${e.message}"
                }
            }
        }
    }
}