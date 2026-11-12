
pipeline {
    agent any

    stages {

        stage('CHECKOUT GIT') {
            steps {
                echo 'Getting student-management from GitHub'
                checkout scm
            }
        }

        stage('MVN CLEAN') {
            steps {
                bat 'mvnw.cmd clean'
            }
        }

        stage('COMPILE') {
            steps {
                bat 'mvnw.cmd compile'
            }
        }

        stage('UNIT TESTS') {
            steps {
                bat 'mvnw.cmd test'
            }
        }

        stage('JACOCO REPORT') {
            steps {
                bat 'mvnw.cmd verify'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: 'target/surefire-reports/*.xml'
        }

        success {
            echo 'Student Management Pipeline SUCCESS!'
        }

        failure {
            echo 'Pipeline FAILED. Check Console Output.'
        }
    }
}
