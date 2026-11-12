
pipeline {
    agent any

    environment {
        // Nexus : nom du service Docker Compose
        NEXUS_URL = 'http://nexus:8081'
        NEXUS_REPOSITORY = 'student-snapshots'

        // SonarQube : nom du service Docker Compose
        SONAR_URL = 'http://sonarqube:9000'
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

        stage('3 - Tests unitaires') {
            steps {
                echo 'Execution des tests JUnit et generation du rapport JaCoCo'
                sh './mvnw verify'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('4 - SonarQube Analysis') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonar-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh '''
                        ./mvnw sonar:sonar \
                        -Dsonar.host.url=$SONAR_URL \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.projectKey=student-management \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('5 - Deploy to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh '''
                        set +x
                        umask 077
                        mkdir -p ~/.m2

                        cat > ~/.m2/settings.xml <<EOF
<settings>
    <servers>
        <server>
            <id>nexus-snapshots</id>
            <username>${NEXUS_USER}</username>
            <password>${NEXUS_PASSWORD}</password>
        </server>
    </servers>
</settings>
EOF

                        ./mvnw deploy -DskipTests \
                        -Dnexus.url=$NEXUS_URL
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Build, tests, SonarQube et Nexus termines !'
        }

        failure {
            echo 'FAILURE: Consultez les logs de la stage en echec.'
        }

        always {
            echo 'Pipeline termine.'
        }
    }
}
