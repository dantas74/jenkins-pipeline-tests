def GITHUB_STATUS_MAP = [
  'SUCCESS': 'success',
  'FAILURE': 'failure',
]

pipeline {
  agent any

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '', numToKeepStr: '5')
    disableConcurrentBuilds()
  }

  parameters {
    string(name: 'branchName', defaultValue: '')
    string(name: 'projectName', defaultValue: '')
    string(name: 'sshKey', defaultValue: '')
    string(name: 'githubWebhookToken', defaultValue: '')
    string(name: 'baseRef', defaultValue: '')
  }

  stages {
    stage('Setup') {
      steps {
        script {
          env.checkoutBranch = params.baseRef == 'main' ? 'main' : 'develop'
        }
      }
    }

    stage('Checkout SCM') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: "*/${env.checkoutBranch}"]],
          extensions: [[$class: 'CleanCheckout']],
          doGenerateSubmoduleConfigurations: false,
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: params.sshKey, url: GIT_URL]]
        ])
      }
    }

    stage('Merging branches') {
      steps {
        // This is made to get these repositories in local
        sh "git checkout ${params.branchName} && git checkout ${env.checkoutBranch}"

        // This is made to delete last merge-branch of previous result
        sh 'git branch | grep merge-branch > ./tmp.txt; if [[ "$(< ./tmp.txt)" == "  merge-branch" ]]; then git branch -d merge-branch; fi; rm -rf ./tmp.txt'

        // This creates a new version simulating the dev branch with changes proposed in PR
        sh 'git checkout -b merge-branch'
        sh "git merge ${params.branchName}"
      }
    }

    stage('Cleaning old dependencies and cache') {
      steps {
        sh 'composer clearcache'
        sh 'rm -rf vendor/*'
      }
    }

    stage('Installing dependencies') {
      steps {
        sh 'composer install'
      }
    }

    stage('Building image with docker') {
      steps {
        sh 'docker build .'
      }
    }

    stage('Testing project') {
      steps {
        sh './vendor/bin/phpunit'
      }
    }
  }

  post {
    always {
      withCredentials([string(credentialsId: params.githubWebhookToken, variable: 'TOKEN')]) {
        script {
          env.githubStatus = GITHUB_STATUS_MAP[currentBuild.currentResult]
        }

        sh '''
          set +x
          curl "https://api.GitHub.com/repos/matheus-dr/test-multibranch/statuses/$GIT_COMMIT" \
            -H "Content-Type: application/json" \
            -H "Accept: application/vnd.github+json" \
            -H "Authorization: token $TOKEN" \
            -X POST \
            -d '{"state": "'"$githubStatus"'", "context": "CI - Merging branches", "description": "Testing if the merging of the branches will pass in continuous integration", "target_url": "'"$BUILD_URL"'console"}'
        '''
      }
    }
  }
}