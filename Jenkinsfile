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
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Build Docker image') {
            steps {
                //sh 'docker buildx build --platform linux/arm64 -t $DOCKER_IMAGE -f docker/Dockerfile --load . --no-cache'
                sh 'docker build -t jgf78/notificator:latest -f docker/Dockerfile . '
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
        
        stage('Deploy to Docker') {
            steps {
                sh '''
                    docker stop notificator || true
                    docker rm notificator || true
                    docker pull jgf78/notificator:latest
                    docker run -d -p 8083:8081 --name notificator jgf78/notificator:latest
                '''
            }
        }
    }

    post {
        success { echo '✅ Build y push completado correctamente' }
        failure { echo '❌ Error en el pipeline' }
    }
}
