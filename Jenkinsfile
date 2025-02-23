pipeline {
    agent any

    tools {
        gradle 'gradle'
    }

    environment {
        imageName = credentials('IMAGE_NAME')
        registryCredential = credentials('REGISTRY_CREDENTIAL')

        releaseServerAccount = credentials('RELEASE_SERVER_ACCOUNT')
        releaseServerUri = credentials('RELEASE_SERVER_URI')
        releasePort = credentials('RELEASE_SERVER_PORT')

        githubTargetBranch = credentials('GITHUB_TARGET_BRANCH')
        githubCloneUrl = credentials('GITHUB_CLONE_URL')
    }

    post {
        success {
            githubCommitStatus context: 'build', state: 'success', description: 'Build succeeded', targetUrl: "${BUILD_URL}"
        }
        failure {
            githubCommitStatus context: 'build', state: 'failure', description: 'Build failed', targetUrl: "${BUILD_URL}"
        }
    }

    stages {
        stage('Get Merge Request and preBuildMerge') {
            steps {
                cleanWs()
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "origin/develop"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'PreBuildMerge', options: [mergeRemote: 'origin', mergeTarget: '$githubTargetBranch']]],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: 'github_token', url: '$githubCloneUrl']]
                    ])
                }

            }
        }

        stage('Setup build environment') {
            steps {
                script {
                        sh 'rm -rf tl1p/env'
                        sh 'mkdir -p tl1p/env'
                        sh "chown -R jenkins:jenkins tl1p/env"
                        sh "chmod -R 755 tl1p/env"
                }


                withCredentials([file(credentialsId: 'db', variable: 'dbFile')]) {
                    script {
                        sh 'cp $dbFile tl1p/env/db.env'
                    }
                }


                withCredentials([file(credentialsId: 'test-db', variable: 'testdbFile')]) {
                    script {
                        sh 'cp $testdbFile tl1p/env/test-db.env'
                    }
                }

                withCredentials([file(credentialsId: 'security', variable: 'securityFile')]) {
                    script {
                        sh 'cp $securityFile tl1p/env/security.env'
                    }
                }

                withCredentials([file(credentialsId: 'application.yml', variable: 'ymlFile')]) {
                     script {
                         sh 'cp $ymlFile tl1p/src/main/resources/application.yml'
                     }
                }

            }
        }

        stage('Build') {
            steps {
                echo 'Building...'
                 dir('tl1p') {
                    sh 'chmod +x ./gradlew'
                    sh './gradlew clean bootJar'
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Test...'
                dir('tl1p') {
                    sh './gradlew test'
                }
            }
        }

        stage('[Backend] Image Build & DockerHub Push') {
            when {
                expression { env.GITHUB_PR_STATE != 'opened' }
            }
            steps {
                dir('tl1p') {
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
        }

        stage('DockerHub Pull') {
            when {
                expression { env.GITHUB_PR_STATE != 'opened' }
            }
            steps {
                sshagent(credentials: ['ubuntu_key']) {
                    sh "ssh -o StrictHostKeyChecking=no $releaseServerAccount@$releaseServerUri 'sudo docker pull $imageName:latest'"
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
                        ssh -o StrictHostKeyChecking=no $releaseServerAccount@$releaseServerUri "sudo docker run -i -e TZ=Asia/Seoul --name tl1p -p releasePort:releasePort -d $imageName:latest"
                    """
                }
            }
        }
    }
}