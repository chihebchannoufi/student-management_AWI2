// Credentials AWS du Learner Lab (portee limitee aux blocs withCredentials)
def awsCreds = [
    string(credentialsId: 'aws-access-key-id',     variable: 'AWS_ACCESS_KEY_ID'),
    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
    string(credentialsId: 'aws-session-token',     variable: 'AWS_SESSION_TOKEN')
]

pipeline {
    agent any

    options {
        timestamps()
        // Evite le stage automatique "Declarative: Checkout SCM"
        skipDefaultCheckout(true)
    }

    environment {
        terraformDir = 'terraform'
        clusterName  = 'student-eks'
        awsRegion    = 'us-east-1'
        KUBECONFIG   = "${WORKSPACE}\\kubeconfig"
    }

    parameters {
        booleanParam(name: 'TERRAFORM_DESTROY', defaultValue: false, description: 'Cochez pour detruire l infrastructure AWS Terraform')
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

        // DEMARRE le conteneur local, verifie qu'il tourne, puis publie l'image sur Docker Hub
        stage('DEPLOY OUR IMAGE') {
            steps {
                bat(returnStatus: true, script: 'docker rm -f student-management')

                bat '''
                docker run -d --name student-management -p 8089:8080 ^
                  -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/studentdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" ^
                  -e "SPRING_DATASOURCE_USERNAME=root" ^
                  -e "SPRING_DATASOURCE_PASSWORD=" ^
                  student-management:latest
                '''

                sleep time: 25, unit: 'SECONDS'

                bat 'docker ps --filter "name=student-management" --filter "status=running" --format "{{.Names}}" | findstr student-management || (docker logs student-management --tail 40 & exit /b 1)'

                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')]) {
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

        // VERIFIE la connexion AWS avec les 3 credentials Secret text
        stage('Test AWS Credentials') {
            steps {
                withCredentials(awsCreds) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        bat 'aws sts get-caller-identity'
                    }
                }
            }
        }

        // PREPARE le cluster EKS et ses noeuds avec Terraform
        stage('SETUP TERRAFORM') {
            when { expression { return !params.TERRAFORM_DESTROY } }
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

        // CONFIGURE la kubeconfig du workspace pour acceder au cluster student-eks
        stage('UPDATE KUBECONFIG') {
            when { expression { return !params.TERRAFORM_DESTROY } }
            steps {
                withCredentials(awsCreds) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        bat 'aws eks update-kubeconfig --name %clusterName% --region %awsRegion% --kubeconfig "%KUBECONFIG%"'
                    }
                }
            }
        }

        // DEPLOIE MySQL et l'application sur Kubernetes AWS puis affiche leur statut
        stage('DEPLOY TO AWS KUBERNETES') {
            when { expression { return !params.TERRAFORM_DESTROY } }
            steps {
                withCredentials(awsCreds + [
                    string(credentialsId: 'mysql-root-password', variable: 'MYSQL_ROOT_PASSWORD'),
                    usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')
                ]) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        bat '''
                        aws sts get-caller-identity
                        copy k8s\\app.yaml k8s\\app.yaml.template
                        powershell -NoProfile -Command "$text = [IO.File]::ReadAllText('k8s/app.yaml'); [IO.File]::WriteAllText('k8s/app.yaml', $text.Replace('<DOCKERHUB_USER>', $env:DOCKERHUB_USERNAME))"
                        kubectl create secret generic mysql-root-secret --from-literal=mysql-root-password="%MYSQL_ROOT_PASSWORD%" --dry-run=client -o yaml | kubectl apply -f -
                        kubectl apply -f k8s/
                        kubectl rollout status deployment/student-management --timeout=300s
                        kubectl get pods,svc
                        copy k8s\\app.yaml.template k8s\\app.yaml
                        del k8s\\app.yaml.template
                        '''
                    }
                }
            }
            post {
                always {
                    // RESTAURE app.yaml meme si le deploiement a echoue
                    bat '''
                    if exist k8s\\app.yaml.template (
                        copy k8s\\app.yaml.template k8s\\app.yaml
                        del k8s\\app.yaml.template
                    )
                    '''
                }
            }
        }

        // DETRUIT l'infrastructure uniquement si TERRAFORM_DESTROY est coche
        stage('TERRAFORM DESTROY') {
            when { expression { return params.TERRAFORM_DESTROY } }
            steps {
                withCredentials(awsCreds) {
                    withEnv(["AWS_DEFAULT_REGION=${env.awsRegion}"]) {
                        dir(terraformDir) {
                            bat 'terraform init'
                            bat 'terraform destroy -auto-approve'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline termine avec succes : build, tests, analyse, Nexus, Docker et AWS.'
        }
        failure {
            echo 'Pipeline en echec : consultez les logs du stage ou les rapports JUnit.'
        }
    }
}