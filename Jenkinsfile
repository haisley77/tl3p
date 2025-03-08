pipeline {
    agent any

    tools {
        gradle 'gradle'
    }

    environment {
        imageName = 'haisley77/release'
        registryCredential = 'docker_token'

        releaseServerAccount = credentials('RELEASE_SERVER_ACCOUNT')
        releaseServerUri = credentials('RELEASE_SERVER_URI')
        releasePort = credentials('RELEASE_SERVER_PORT')

        githubTargetBranch = 'release/v1.0'
        githubCloneUrl = credentials('GITHUB_CLONE_URL')
    }

    post {
        success {
            echo 'deployment success'
        }
        failure {
            echo 'deployment failure'
        }
    }


    stages {
        stage('Git Clone') {
            steps {
                git branch: "${githubTargetBranch}", credentialsId: 'github_token',
                url: "${env.githubCloneUrl}"

            }
        }

        stage('Setup build environment') {
            steps {
                script {
                        sh 'rm -rf env'
                        sh 'mkdir -p env'
                        sh "chown -R jenkins:jenkins env"
                        sh "chmod -R 755 env"
                        sh 'rm -rf src/main/resources'
                        sh 'mkdir -p src/main/resources'
                        sh "chown -R jenkins:jenkins src/main/resources"
                        sh "chmod -R 755 src/main/resources"
                }

                withCredentials([file(credentialsId: 'log', variable: 'logFile')]) {
                    script {
                        sh 'cp $logFile src/resources/logback-spring.xml'
                    }
                }


                withCredentials([file(credentialsId: 'db', variable: 'dbFile')]) {
                    script {
                        sh 'cp $dbFile env/db.env'
                    }
                }


                withCredentials([file(credentialsId: 'test-db', variable: 'testdbFile')]) {
                    script {
                        sh 'cp $testdbFile env/test-db.env'
                    }
                }

                withCredentials([file(credentialsId: 'security', variable: 'securityFile')]) {
                    script {
                        sh 'cp $securityFile env/security.env'
                    }
                }

                withCredentials([file(credentialsId: 'application.yml', variable: 'ymlFile')]) {
                     script {
                         sh 'cp $ymlFile src/main/resources/application.yml'
                     }
                }

            }
        }

        stage('Build') {
            steps {
                echo 'Building...'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean bootJar'
            }
        }

        stage('Test') {
            steps {
                echo 'Test...'
                sh './gradlew test'
            }
        }

        stage('[Backend] Image Build & DockerHub Push') {
            when {
                expression { env.GITHUB_PR_STATE != 'opened' }
            }
            steps {
                script {
                    docker.withRegistry('', registryCredential) {
                        // sh "docker build -t $imageName:$BUILD_NUMBER ."
                        sh "docker build -t $imageName:latest ."

                        // sh "docker push $imageName:$BUILD_NUMBER"
                        sh "docker push $imageName:latest"
                    }
                }
            }
        }

        stage('DockerHub Pull') {
            when {
                expression { env.GITHUB_PR_STATE != 'opened' }
            }
            steps {
                sshagent(credentials: ['ubuntu_key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${env.releaseServerAccount}@${env.releaseServerUri} 'sudo docker pull $imageName:latest'
                    """
                }
            }
        }

        stage('Service Start') {
            when {
                expression { env.GITHUB_PR_STATE != 'opened' }
            }
            steps {
                sshagent(credentials: ['ubuntu_key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${env.releaseServerAccount}@${env.releaseServerUri} '
                            if [ \$(docker ps -q --filter "name=tl1p") ]; then
                                docker stop tl1p
                                docker rm tl1p
                            fi

                            sudo docker run -i -e TZ=Asia/Seoul \
                                --env-file=/home/ubuntu/env/db.env \
                                --env-file=/home/ubuntu/env/security.env \
                                --env-file=/home/ubuntu/env/test-db.env \
                                --name tl1p \
                                -p ${env.releasePort}:${env.releasePort} \
                                -d $imageName:latest
                        '
                    """
                }
            }
        }


    }
}