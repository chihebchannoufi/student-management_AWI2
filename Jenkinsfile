pipeline {
    agent any

    environment {
        SONAR_URL = 'http://sonarqube:9000'
        NEXUS_URL = 'http://nexus:8081/repository/maven-snapshots/'
    }

    stages {
        stage('CHECKOUT GIT') {
            steps {
                echo 'Recuperation du projet depuis GitHub'
                checkout scm
            }
        }

        stage('MVN CLEAN') {
            steps {
                echo 'Nettoyage du projet'
                sh 'chmod +x mvnw'
                sh './mvnw clean'
            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps {
                echo 'Compilation du projet'
                sh './mvnw compile'
            }
        }

        stage('UNIT TESTS') {
            steps {
                echo 'Execution des tests unitaires (Mock)'
                sh './mvnw test -Dtest=DepartmentServiceMockTest'
            }
        }

        stage('MVN SONARQUBE') {
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

        stage('PUBLISH TO NEXUS') {
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

        // --- Nouvelles étapes ajoutées en mode "Placeholder" (bouchon) pour l'affichage UI ---
        
        stage('BUILDING OUR IMAGE') {
            steps {
                echo 'Étape à implémenter : Construction de l image Docker'
            }
        }

        stage('DEPLOY OUR IMAGE') {
            steps {
                echo 'Étape à implémenter : Déploiement de l image'
            }
        }

        stage('Test AWS Credentials') {
            steps {
                echo 'Étape à implémenter : Test des accès AWS'
            }
        }

        stage('SETUP TERRAFORM') {
            steps {
                echo 'Étape à implémenter : Initialisation Terraform'
            }
        }

        stage('UPDATE KUBECONFIG') {
            steps {
                echo 'Étape à implémenter : Mise à jour de Kubernetes'
            }
        }

        stage('DEPLOY TO AWS KUBERNETES') {
            steps {
                echo 'Étape à implémenter : Déploiement final'
            }
        }
    }
}