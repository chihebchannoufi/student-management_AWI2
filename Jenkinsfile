/* Credentials AWS du Learner Lab (portee limitee aux blocs withCredentials) */
def awsCreds = [
    string(credentialsId: 'aws-access-key-id',     variable: 'AWS_ACCESS_KEY_ID'),
    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
    string(credentialsId: 'aws-session-token',     variable: 'AWS_SESSION_TOKEN')
]

pipeline {
    agent any

    options {
        timestamps()
        // Supprime le stage automatique "Declarative: Checkout SCM" (CHECKOUT GIT s'en charge)
        skipDefaultCheckout(true)
    }

    // Declenche le pipeline immediatement a chaque push GitHub (webhook)
    triggers {
        githubPush()
    }

    environment {
        terraformDir = 'terraform'
        clusterName  = 'student-eks'
        awsRegion    = 'us-east-1'
        KUBECONFIG   = "${WORKSPACE}\\kubeconfig"
    }

    stages {

        // RECUPERE le code source depuis le depot Git
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

        // CONSTRUIT l'application Spring Boot
        stage('ARTIFACT CONSTRUCTION') {
            steps {
                bat '.\\mvnw.cmd package -DskipTests'
            }
        }

        // EXECUTE les tests unitaires et conserve les rapports JUnit
        stage('UNIT TESTS') {
            steps {
                bat '.\\mvnw.cmd test'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // ANALYSE la qualite du code avec SonarQube
        stage('MVN SONARQUBE') {
            steps {
                withCredentials([
                    string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')
                ]) {
                    withSonarQubeEnv('SonarQube') {
                        bat '''
                        .\\mvnw.cmd verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar ^
                        -Dsonar.projectKey=student-management ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.token=%SONAR_TOKEN%
                        '''
                    }
                }
            }
        }

        // PUBLIE l'artefact dans Nexus
        stage('PUBLISH TO NEXUS') {
            steps {
                configFileProvider([
                    configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')
                ]) {
                    bat '.\\mvnw.cmd deploy -DskipTests -s "%MAVEN_SETTINGS%"'
                }
            }
        }

        // CONSTRUIT les images Docker
        stage('BUILDING OUR IMAGE') {
            steps {
                bat 'docker build -t student-management:%BUILD_NUMBER% -t student-management:latest .'
            }
        }

        // DEPLOIE localement et publie sur Docker Hub
        stage('DEPLOY OUR IMAGE') {
            steps {
                // Ignore si aucun ancien conteneur n'existe
                bat(returnStatus: true, script: 'docker rm -f student-management')

                // Demarre le conteneur Spring Boot
                bat '''
                docker run -d --name student-management -p 8089:8080 ^
                  -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/studentdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" ^
                  -e "SPRING_DATASOURCE_USERNAME=root" ^
                  -e "SPRING_DATASOURCE_PASSWORD=" ^
                  student-management:latest
                '''

                // Attend le demarrage
                sleep time: 25, unit: 'SECONDS'

                // Verifie que le conteneur est actif
                bat '''
                docker ps --filter "name=student-management" --filter "status=running" --format "{{.Names}}" | findstr student-management || (docker logs student-management --tail 40 & exit /b 1)
                '''

                // Publie sur Docker Hub
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
                    bat '''
                    powershell -NoProfile -Command "Write-Output $env:DOCKERHUB_PASSWORD | docker login -u $env:DOCKERHUB_USERNAME --password-stdin"

                    docker tag student-management:latest %DOCKERHUB_USERNAME%/student-management:latest
                    docker tag student-management:latest %DOCKERHUB_USERNAME%/student-management:%BUILD_NUMBER%

                    docker push %DOCKERHUB_USERNAME%/student-management:latest
                    docker push %DOCKERHUB_USERNAME%/student-management:%BUILD_NUMBER%

                    docker logout
                    '''
                }
            }
        }

        // VERIFIE les credentials AWS
        stage('Test AWS Credentials') {
            steps {
                withCredentials(awsCreds) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        bat 'aws sts get-caller-identity'
                    }
                }
            }
        }

        // PREPARE le cluster EKS avec Terraform
        stage('SETUP TERRAFORM') {
            steps {
                withCredentials(awsCreds) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        dir(terraformDir) {
                            bat 'terraform init'
                            bat 'terraform validate'
                            bat 'terraform apply -auto-approve'
                        }
                    }
                }
            }
        }

        // CONFIGURE le kubeconfig
        stage('UPDATE KUBECONFIG') {
            steps {
                withCredentials(awsCreds) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        bat 'aws eks update-kubeconfig --name %clusterName% --region %awsRegion% --kubeconfig "%KUBECONFIG%"'
                    }
                }
            }
        }

        // DEPLOIE MySQL et l'application sur Kubernetes (echoue si le deploiement n'aboutit pas)
        stage('DEPLOY TO AWS KUBERNETES') {
            steps {
                withCredentials(awsCreds + [
                    string(
                        credentialsId: 'mysql-root-password',
                        variable: 'MYSQL_ROOT_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        bat '''
                        aws sts get-caller-identity

                        copy k8s\\app.yaml k8s\\app.yaml.template

                        powershell -NoProfile -Command "$text = [IO.File]::ReadAllText('k8s/app.yaml'); [IO.File]::WriteAllText('k8s/app.yaml', $text.Replace('<DOCKERHUB_USER>', $env:DOCKERHUB_USERNAME))"

                        kubectl create secret generic mysql-root-secret --from-literal=mysql-root-password="%MYSQL_ROOT_PASSWORD%" --dry-run=client -o yaml | kubectl apply -f -

                        kubectl apply -f k8s/
                        if errorlevel 1 exit /b 1

                        kubectl rollout status deployment/student-management --timeout=300s
                        if errorlevel 1 (
                            kubectl get pods,svc
                            kubectl get nodes
                            exit /b 1
                        )

                        kubectl get pods,svc
                        '''
                    }
                }
            }

            // RESTAURE app.yaml meme en cas d'echec du deploiement
            post {
                always {
                    bat '''
                    if exist k8s\\app.yaml.template (
                        copy k8s\\app.yaml.template k8s\\app.yaml
                        del k8s\\app.yaml.template
                    )
                    '''
                }
            }
        }
    }

    // NOTIFICATIONS GLOBALES DU PIPELINE
    post {

        success {
            echo 'Pipeline termine avec succes : build, tests, analyse, Nexus, Docker et AWS.'

            // ENVOIE UN EMAIL DE SUCCES
            emailext(
                subject: "SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                to: 'channoufichiheb@gmail.com',
                body: """
                    <h3 style="color:#2e7d32">Pipeline reussi</h3>
                    <p>
                      Projet : <b>${env.JOB_NAME}</b><br>
                      Build : <b>#${env.BUILD_NUMBER}</b><br>
                      Statut : <b>SUCCESS</b><br>
                      Duree : ${currentBuild.durationString.replace(' and counting', '')}
                    </p>
                    <p>Etapes validees : build, tests, SonarQube, Nexus, Docker et AWS EKS.</p>
                    <p><a href="${env.BUILD_URL}">Ouvrir le build</a></p>
                """
            )
        }

        failure {
            echo 'Pipeline en echec : consultez les logs du stage ou les rapports JUnit.'

            // ENVOIE UN EMAIL D'ECHEC AVEC LE JOURNAL DU BUILD EN PIECE JOINTE
            emailext(
                subject: "FAILURE - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                to: 'channoufichiheb@gmail.com',
                attachLog: true,
                compressLog: true,
                body: """
                    <h3 style="color:#c62828">Pipeline en echec</h3>
                    <p>
                      Projet : <b>${env.JOB_NAME}</b><br>
                      Build : <b>#${env.BUILD_NUMBER}</b><br>
                      Statut : <b>FAILURE</b>
                    </p>
                    <p><a href="${env.BUILD_URL}console">Ouvrir la console du build</a></p>
                    <p>Le journal complet du build est joint a ce message.</p>
                """
            )
        }

        always {
            echo "Pipeline termine. Statut final : ${currentBuild.currentResult}"
        }
    }
}