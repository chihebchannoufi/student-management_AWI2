pipeline {
    agent any

    environment {
        SONAR_URL = 'http://sonarqube:9000'
        NEXUS_URL = 'http://nexus:8081/repository/maven-snapshots/'
    }

    stages {
        stage('1 - Checkout') {
            steps {
                echo 'Recuperation du projet depuis GitHub'
                checkout scm
            }
        }

        stage('2 - Build') {
            steps {
                echo 'Compilation du projet'
                sh 'chmod +x mvnw'
                sh './mvnw clean compile'
            }
        }

        stage('3 - Unit Tests') {
            steps {
                echo 'Execution des tests unitaires (Mock)'
                // On execute uniquement le test Mock pour ne pas bloquer sur MySQL/WampServer dans Docker
                sh './mvnw test -Dtest=DepartmentServiceMockTest'
            }
        }

        stage('4 - SonarQube Analysis') {
            steps {
                echo 'Analyse de la qualite du code avec SonarQube'
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        ./mvnw jacoco:report sonar:sonar \
                        -Dsonar.projectKey=student-management \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('5 - Deploy to Nexus') {
            steps {
                echo 'Deploiement du .jar dans Nexus'
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-credentials',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh '''
                        printf "<settings><servers><server><id>nexus-snapshots</id><username>%s</username><password>%s</password></server></servers></settings>" "${NEXUS_USER}" "${NEXUS_PASS}" > nexus-settings.xml
                        ./mvnw deploy -s nexus-settings.xml -DskipTests \
                        -DaltDeploymentRepository=nexus-snapshots::default::${NEXUS_URL}
                        rm -f nexus-settings.xml
                    '''
                }
            }
        }
    }
}