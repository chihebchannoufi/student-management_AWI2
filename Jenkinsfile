pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        // RECUPERE le code source depuis le depot Git configure dans Jenkins
        stage('CHECKOUT GIT') {
            steps {
                checkout scm
            }
        }

        // NETTOIE les anciens fichiers de compilation Maven
        stage('MVN CLEAN') {
            steps {
                bat '.\\mvnw.cmd clean'
            }
        }

        // CONSTRUIT l'application Spring Boot sans executer les tests
        stage('ARTIFACT CONSTRUCTION') {
            steps {
                bat '.\\mvnw.cmd package -DskipTests'
            }
        }

        // EXECUTE les tests unitaires et conserve leurs rapports JUnit
        stage('UNIT TESTS') {
            steps {
                bat '.\\mvnw.cmd test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // ANALYSE la qualite du code avec SonarQube
        stage('MVN SONARQUBE') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        bat '.\\mvnw.cmd verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=student-management -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%SONAR_TOKEN%'
                    }
                }
            }
        }

        // PUBLIe l'artefact SNAPSHOT ou RELEASE dans Nexus
        stage('PUBLISH TO NEXUS') {
            steps {
                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    bat '.\\mvnw.cmd deploy -DskipTests -s "%MAVEN_SETTINGS%"'
                }
            }
        }

        // CONSTRUIT les images Docker taguees avec le build et latest
        stage('BUILDING OUR IMAGE') {
            steps {
                bat 'docker build -t student-management:%BUILD_NUMBER% -t student-management:latest .'
            }
        }

        // ARRETE l'ancien conteneur puis demarre la nouvelle image
        stage('DEPLOY OUR IMAGE') {
            steps {
                bat 'docker stop student-management >nul 2>&1 || echo Aucun conteneur student-management actif'
                bat 'docker rm student-management >nul 2>&1 || echo Aucun ancien conteneur student-management'
                bat 'docker run -d --name student-management -p 8089:8080 student-management:latest'
            }
        }

        /*
        // Les stages AWS restent descriptes uniquement et seront actives plus tard
        stage('Test AWS Credentials') {
            steps {
                // TODO
            }
        }

        // PREPARE l'environnement Terraform pour le deploiement AWS
        stage('SETUP TERRAFORM') {
            steps {
                // TODO
            }
        }

        // CONFIGURE l'acces au cluster Kubernetes AWS
        stage('UPDATE KUBECONFIG') {
            steps {
                // TODO
            }
        }

        // DEPLOIE l'application sur Kubernetes AWS
        stage('DEPLOY TO AWS KUBERNETES') {
            steps {
                // TODO
            }
        }
        */
    }

    post {
        success {
            echo 'Pipeline termine avec succes : build, tests, analyse, Nexus et Docker.'
        }
        failure {
            echo 'Pipeline en echec : consultez les logs du stage ou les rapports JUnit.'
        }
    }
}
