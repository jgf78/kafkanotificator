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
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Build Docker image') {
            steps {
                // Construcción normal, compatible con ARM y sin BuildKit
                sh 'docker build --no-cache -t $DOCKER_IMAGE -f docker/Dockerfile .'
            }
        }

        stage('Push Docker image') {
            steps {
                // Usamos credentialsId 'dockerhub' que debe contener usuario y token de Docker Hub
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_TOKEN')]) {
                    // Login seguro usando token
                    sh 'echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USER" --password-stdin'
                    // Push de la imagen
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