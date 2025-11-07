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
                // Ejecutamos Maven directamente en el Jenkins host
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Build Docker image') {
            steps {
                // Usamos buildx para ARM y sin cache
                sh 'docker buildx build --no-cache --platform linux/arm64 -t $DOCKER_IMAGE -f docker/Dockerfile --load .'
            }
        }

        stage('Push Docker image') {
            steps {
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
