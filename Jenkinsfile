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
            when {
                branch 'main'
            }
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

        stage('Copy Jar') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    echo "🔧 Sincronizando workspace..."
                    mkdir -p target
                    cp -fv ${WORKSPACE}@2/target/*.jar target/
                    ls -la target
                '''
            }
        }

        stage('Build Docker image') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    echo "🐳 Construyendo imagen Docker SIN CACHE..."
                    docker build --no-cache -t jgf78/notificator:latest -f docker/Dockerfile .
                '''
            }
        }

        stage('Push Docker image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_TOKEN')]) {
                    sh '''
                        echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKER_IMAGE
                    '''
                }
            }
        }

        stage('Deploy to Docker') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    docker stop notificator || true
                    docker rm notificator || true
                    docker pull jgf78/notificator:latest
                    docker run -d --restart unless-stopped -p 8083:8081 -e SPRING_DATA_REDIS_HOST=redis-cache -e SPRING_DATA_REDIS_PORT=6379 --network notificator-net --name notificator jgf78/notificator:latest
                '''
            }
        }
    }

    post {
        success { echo '✅ Build, push y despliegue completado correctamente' }
        failure { echo '❌ Error en el pipeline' }
    }
}
