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

        // PUBLIE l'artefact SNAPSHOT ou RELEASE dans Nexus
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

        // ARRETE l'ancien conteneur, demarre la nouvelle image avec MySQL (root sans mot de passe), puis verifie qu'elle tourne
        stage('DEPLOY OUR IMAGE') {
            steps {
                bat 'docker stop student-management >nul 2>&1 || echo Aucun conteneur student-management actif'
                bat 'docker rm student-management >nul 2>&1 || echo Aucun ancien conteneur student-management'

                bat '''
                docker run -d --name student-management -p 8089:8080 ^
                  -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/studentdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" ^
                  -e "SPRING_DATASOURCE_USERNAME=root" ^
                  -e "SPRING_DATASOURCE_PASSWORD=" ^
                  student-management:latest
                '''

                // ATTEND le demarrage de Spring Boot
                sleep time: 25, unit: 'SECONDS'

                // VERIFIE que le conteneur est toujours actif, sinon affiche ses logs et echoue
                bat 'docker ps --filter "name=student-management" --filter "status=running" --format "{{.Names}}" | findstr student-management || (docker logs student-management --tail 40 & exit /b 1)'
            }
        }

        /*
        // Les stages AWS restent decrits uniquement et seront actives plus tard
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