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

        stage('Copy Jar') {
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

        stage('Deploy with Docker Compose') {
            steps {
                sh '''
                    echo "🚀 Desplegando stack con Docker Compose..."
                    DOCKER_COMPOSE_FILE="${WORKSPACE}/docker/docker-compose.yml"
        
                    # Intenta con el guion si el espacio falla
                    docker-compose -f $DOCKER_COMPOSE_FILE pull
                    docker-compose -f $DOCKER_COMPOSE_FILE up -d
                '''
            }
        }

    }

    post {
        success { echo '✅ Build, push y despliegue completado correctamente' }
        failure { echo '❌ Error en el pipeline' }
    }
}
