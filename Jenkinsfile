pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        terraformDir = 'terraform'
        awsCredentialsId = 'aws-credentials'
        clusterName = 'student-eks'
        awsRegion = 'us-east-1'
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

        // SUPPRIME l'ancien conteneur, demarre la nouvelle image avec MySQL, verifie qu'elle tourne, puis la publie sur Docker Hub
        stage('DEPLOY OUR IMAGE') {
            steps {
                // returnStatus: true ignore le code de retour si aucun conteneur n'existe
                bat(returnStatus: true, script: 'docker rm -f student-management')

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

                // PUBLIE la meme image sur Docker Hub avec les tags latest et numerote
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

        // LIT le fichier de credentials AWS Learner Lab, charge les variables d'environnement et verifie l'identite
        stage('Test AWS Credentials') {
            steps {
                withCredentials([file(credentialsId: awsCredentialsId, variable: 'AWS_CREDENTIALS_FILE')]) {
                    script {
                        // Lecture du fichier de credentials AWS
                        def lines = readFile(AWS_CREDENTIALS_FILE).readLines()

                        // Extraction d'une valeur "cle=valeur" (split limite a 2 pour garder les '=' du token)
                        def getValue = { String key ->
                            def line = lines.find { it.trim().startsWith(key) }
                            return line ? line.split('=', 2)[1].trim() : null
                        }

                        def accessKey = getValue('aws_access_key_id')
                        def secretKey = getValue('aws_secret_access_key')
                        def sessionToken = getValue('aws_session_token')

                        if (!accessKey || !secretKey || !sessionToken) {
                            error 'Fichier aws-credentials incomplet : aws_access_key_id, aws_secret_access_key et aws_session_token sont obligatoires.'
                        }

                        // Variables AWS visibles par tous les stages suivants (jamais affichees dans les logs)
                        env.AWS_ACCESS_KEY_ID = accessKey
                        env.AWS_SECRET_ACCESS_KEY = secretKey
                        env.AWS_SESSION_TOKEN = sessionToken
                        env.AWS_DEFAULT_REGION = awsRegion

                        // Kubeconfig propre au workspace Jenkins (pas de dependance au profil utilisateur)
                        env.KUBECONFIG = "${env.WORKSPACE}\\kubeconfig"

                        echo 'AWS Credentials File Loaded'
                    }
                }
                bat 'aws sts get-caller-identity'
            }
        }

        // PREPARE le cluster EKS et ses noeuds avec Terraform dans le dossier terraform
        stage('SETUP TERRAFORM') {
            when {
                expression { return !params.TERRAFORM_DESTROY }
            }
            steps {
                echo "Avant d'entrer dans le repertoire Terraform"
                dir(terraformDir) {
                    echo "Dans le repertoire Terraform"
                    bat 'terraform init'
                    bat 'terraform validate'
                    bat 'terraform apply -auto-approve'
                }
            }
        }

        // CONFIGURE la kubeconfig du workspace pour acceder au cluster student-eks
        stage('UPDATE KUBECONFIG') {
            when {
                expression { return !params.TERRAFORM_DESTROY }
            }
            steps {
                bat 'aws eks update-kubeconfig --name %clusterName% --region %awsRegion% --kubeconfig "%KUBECONFIG%"'
            }
        }

        // DEPLOIE MySQL et l'application sur Kubernetes AWS puis affiche leur statut
        stage('DEPLOY TO AWS KUBERNETES') {
            when {
                expression { return !params.TERRAFORM_DESTROY }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'mysql-root-password', variable: 'MYSQL_ROOT_PASSWORD'),
                    usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')
                ]) {
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

        // DETRUIT l'infrastructure uniquement si le parametre TERRAFORM_DESTROY est coche
        stage('TERRAFORM DESTROY') {
            when {
                expression { return params.TERRAFORM_DESTROY }
            }
            steps {
                dir(terraformDir) {
                    bat 'terraform init'
                    bat 'terraform destroy -auto-approve'
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