pipeline {
    agent any

    stages {

        stage('TEST') {
            steps {
                echo 'hook checked ...'
            }
        }

        stage('Git clone') {
            steps {
                git branch: 'develop', credentialsId: 'github_token',
                url: 'https://github.com/haisley77/tl1p.git'
            }
        }
    }

    post {
        success {
            githubPRStatusPublisher statusMsg: githubPRMessage('${GITHUB_PR_COND_REF} run ended'), unstableAs: 'SUCCESS'
        }
        failure {
            githubPRStatusPublisher statusMsg: githubPRMessage('${GITHUB_PR_COND_REF} run ended'), unstableAs: 'FAILURE'
        }
    }

}