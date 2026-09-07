pipeline {
    agent any
    triggers { pollSCM('H/2 * * * *') }
    environment {
        IMAGE = 'fitmeasure-app'
        CONTAINER = 'fitmeasure_app'
    }
    stages {
        stage('Build JAR') {
            steps {
                sh '''
                HOST_WS=$(echo "$WORKSPACE" | sed "s|/var/jenkins_home|/volume1/docker/jenkins_home|")
                docker run --rm \
                  -v "$HOST_WS":/src \
                  -v /volume1/docker/maven-repo:/root/.m2 \
                  -w /src \
                  maven:3.9-eclipse-temurin-21 \
                  mvn clean package -DskipTests
                '''
            }
        }
        stage('Build Image') {
            steps { sh 'docker build -t $IMAGE .' }
        }
        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'fitmeasure-db-password', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'fitmeasure_KAKAO_CLIENT_ID', variable: 'KAKAO_CLIENT_ID'),
                    string(credentialsId: 'fitmeasure_KAKAO_CLIENT_SECRET', variable: 'KAKAO_CLIENT_SECRET')
                ]) {
                    sh '''
                    docker rm -f $CONTAINER || true
                    docker run -d --name $CONTAINER \
                      --network iamnot-net \
                      -p 10341:8080 \
                      -e DB_URL="jdbc:mysql://iamnotmeeting-mysql:3306/fitmeasure?serverTimezone=Asia/Seoul&characterEncoding=UTF-8" \
                      -e DB_USER="fitmeasure" \
                      -e DB_PASSWORD="$DB_PASSWORD" \
                      -e KAKAO_CLIENT_ID="$KAKAO_CLIENT_ID" \
                      -e KAKAO_CLIENT_SECRET="$KAKAO_CLIENT_SECRET" \
                      --restart unless-stopped \
                      $IMAGE
                    '''
                }
            }
        }
    }
}