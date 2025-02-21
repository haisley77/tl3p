pipeline {
    agent any

    stages {

        stage('TEST') {
            steps {
                echo 'hook checked ...'
            }
        }
    }

    post {
        success {
            githubCommitStatus(name: 'prStatus', state: 'success', description: 'SUCCESS')
        }
        failure {
            githubCommitStatus(name: 'prStatus', state: 'failure', description: 'FAIL')
        }
    }

}