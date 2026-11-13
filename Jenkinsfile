
pipeline {
    agent any

    environment {
        SONAR_URL = 'http://sonarqube:9000'
        NEXUS_URL = 'http://nexus:8081'
    }

    stages {

        stage('1 - CHECKOUT GIT') {
            steps {
                echo 'Recuperation du projet depuis GitHub'
                checkout scm
            }
        }

        stage('2 - BUILD') {
            steps {
                echo 'Nettoyage et compilation du projet'
                sh 'chmod +x mvnw'
                sh './mvnw clean compile'
            }
        }

        stage('3 - UNIT TESTS') {
            steps {
                echo 'Execution de tous les tests unitaires'
                sh './mvnw verify'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('4 - SONARQUBE ANALYSIS') {
            steps {
                echo 'Analyse de la qualite du code'
                withCredentials([
                    string(
                        credentialsId: 'sonar-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh '''
                        ./mvnw sonar:sonar \
                        -Dsonar.projectKey=student-management \
                        -Dsonar.host.url=$SONAR_URL \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('5 - PUBLISH TO NEXUS') {
            steps {
                echo 'Publication de l artefact Maven dans Nexus'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {
                    sh '''
                        mkdir -p "$HOME/.m2"

                        cat > "$HOME/.m2/settings.xml" <<EOF
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
                        -DskipTests \
                        -Dnexus.url=$NEXUS_URL

                        rm -f "$HOME/.m2/settings.xml"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS : Build, tests, SonarQube et Nexus termines !'
        }

        failure {
            echo 'FAILURE : Consultez Console Output pour identifier l erreur.'
        }

        always {
            echo 'Pipeline termine.'
        }
    }
}
