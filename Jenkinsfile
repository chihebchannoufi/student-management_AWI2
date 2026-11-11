pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout(true)
    }

    parameters {
        booleanParam(name: 'DEPLOY', defaultValue: false,
                description: 'Deploy the image after a successful build.')
        string(name: 'IMAGE_REPOSITORY', defaultValue: 'student-management',
                description: 'Local image name.')
        string(name: 'DEPLOY_CONTAINER', defaultValue: 'student-management',
                description: 'Name of the deployed Docker container.')
        string(name: 'DEPLOY_PORT', defaultValue: '8089',
                description: 'Host port exposed by the deployed container.')
        string(name: 'DATABASE_URL',
                defaultValue: 'jdbc:mysql://host.docker.internal:3306/studentdb?createDatabaseIfNotExist=true',
                description: 'JDBC URL of the database used by the deployed container.')
        string(name: 'DATABASE_USERNAME', defaultValue: 'root',
                description: 'Database username used by the deployed container.')
        password(name: 'DATABASE_PASSWORD', defaultValue: '',
                description: 'Database password used by the deployed container.')
    }

    environment {
        MYSQL_CONTAINER = 'student-management-mysql-test'
        MYSQL_DATABASE = 'studentdb'
        MYSQL_USERNAME = 'root'
        MYSQL_PASSWORD = 'student-management-test'
        MYSQL_TEST_PORT = '3307'
        LOCAL_IMAGE = "${params.IMAGE_REPOSITORY}:${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Start test database') {
            steps {
                bat '''
                    docker rm -f %MYSQL_CONTAINER% >nul 2>&1 || echo Aucun ancien conteneur
                    docker run --detach --name %MYSQL_CONTAINER% --publish %MYSQL_TEST_PORT%:3306 --env MYSQL_DATABASE=%MYSQL_DATABASE% --env MYSQL_ROOT_PASSWORD=%MYSQL_PASSWORD% mysql:8.4
                    if errorlevel 1 exit /b 1

                    set /a tries=0
                    :wait
                    docker exec %MYSQL_CONTAINER% mysqladmin ping --host=127.0.0.1 --user=root --password=%MYSQL_PASSWORD% --silent >nul 2>&1 && goto ready
                    set /a tries+=1
                    if %tries% GEQ 60 (
                        docker logs %MYSQL_CONTAINER%
                        exit /b 1
                    )
                    ping -n 3 127.0.0.1 >nul
                    goto wait
                    :ready
                    echo MySQL de test pret
                '''
            }
        }

        stage('Test') {
            steps {
                bat '''
                    set SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:%MYSQL_TEST_PORT%/%MYSQL_DATABASE%?createDatabaseIfNotExist=true
                    set SPRING_DATASOURCE_USERNAME=%MYSQL_USERNAME%
                    set SPRING_DATASOURCE_PASSWORD=%MYSQL_PASSWORD%
                    call mvnw.cmd -B clean verify
                '''
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Build image') {
            steps {
                bat 'docker build --tag %LOCAL_IMAGE% .'
            }
        }

        stage('Deploy') {
            when {
                expression { params.DEPLOY }
            }
            steps {
                bat '''
                    docker rm -f %DEPLOY_CONTAINER% >nul 2>&1 || echo Aucun ancien conteneur
                    docker run --detach --name %DEPLOY_CONTAINER% --restart unless-stopped --publish %DEPLOY_PORT%:8089 --add-host host.docker.internal:host-gateway --env SERVER_PORT=8089 --env "SPRING_DATASOURCE_URL=%DATABASE_URL%" --env "SPRING_DATASOURCE_USERNAME=%DATABASE_USERNAME%" --env "SPRING_DATASOURCE_PASSWORD=%DATABASE_PASSWORD%" %LOCAL_IMAGE%
                '''
            }
        }
    }

    post {
        always {
            bat 'docker rm -f %MYSQL_CONTAINER% >nul 2>&1 || echo Nettoyage termine'
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
        success {
            echo "Build completed for ${env.LOCAL_IMAGE}"
        }
    }
}