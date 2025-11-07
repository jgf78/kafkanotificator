pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        DOCKERHUB_USER = 'jgf78'
        DOCKER_IMAGE = 'jgf78/notificator:latest'
    }

    stages {
        stage('Checkout') {
            steps {
                // Usamos las credenciales de GitHub
                checkout([
                    $class: 'GitSCM', 
                    branches: [[name: '*/main']], 
                    userRemoteConfigs: [[
                        url: 'https://github.com/jgf78/kafkanotificator.git',
                        credentialsId: 'github'
                    ]]
                ])
            }
        }

        stage('Build with Maven') {
            steps {
                // Usamos un contenedor Maven oficial para no depender de Maven instalado en Jenkins
                docker.image('maven:3.9-eclipse-temurin-17').inside {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Build Docker image') {
            steps {
                // Construcción normal de Docker, compatible con ARM
                sh 'docker build --no-cache -t $DOCKER_IMAGE -f docker/Dockerfile .'
            }
        }

        stage('Push Docker image') {
            steps {
                // Usamos las credenciales de Docker Hub
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_TOKEN')]) {
                    sh 'echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh 'docker push $DOCKER_IMAGE'
                }
            }
        }
    }

    post {
        success { 
            echo '✅ Build y push completado correctamente' 
        }
        failure { 
            echo '❌ Error en el pipeline' 
        }
    }
}