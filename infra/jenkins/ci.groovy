def ENVIRONMENT_SUFFIX_MAP = [
  'Development': 'dev',
  'Production': 'prod',
]

def ENVIRONMENT_BRANCH_MAP = [
  'Development': '*/develop',
  'Production': '*/main',
]

pipeline {
  agent any

  parameters {
    booleanParam(name: 'isGoingToDeploy', defaultValue: false)
    string(name: 'projectName', defaultValue: '')
    string(name: 'sshKey', defaultValue: '')
    string(name: 'appRegistry', defaultValue: '')
    choice(name: 'environmentParam', choices: ['Development', 'Production'])
    string(name: 'appRegistryUrl', defaultValue: '')
    string(name: 'ecrRegistryCredential', defaultValue: '')
    string(name: 'awsCredentials', defaultValue: '')
    string(name: 'awsRegion', defaultValue: '')
  }

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '', numToKeepStr: '5')
    disableConcurrentBuilds()
  }

  stages {
    stage('Setup') {
      steps {
        script {
          env.branchName = ENVIRONMENT_SUFFIX_MAP[params.environmentParam]
          env.suffix = ENVIRONMENT_BRANCH_MAP[params.environmentParam]
        }
      }
    }

    stage('Checkout SCM') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: env.branch]],
          extensions: [[$class: 'CleanCheckout']],
          doGenerateSubmoduleConfigurations: false,
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: params.sshKey, url: GIT_URL]]
        ])
      }
    }

    /*stage('Cleaning old dependencies and cache') {
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
        sh "docker build -t ${params.appRegistry}:latest-${env.suffix} ."
      }
    }

    stage('Testing project') {
      steps {
        sh './vendor/bin/phpunit'
      }
    }*/

    stage('Debug') {
      steps {
        echo 'It is working :)'
      }
    }

    stage('Enable CD') {
      steps {
        build job: "${env.projectName}/cd",
          parameters: [
            string(name: 'environmentParam', value: 'Development'),
            string(name: 'sshKey', value: params.sshKey),
            string(name: 'projectName', value: params.projectName),
            string(name: 'environmentParam', value: params.environmentParam),
            string(name: 'appRegistryUrl', value: params.appRegistryUrl),
            string(name: 'ecrRegistryCredential', value: params.ecrRegistryCredential),
            string(name: 'awsCredentials', value: params.awsCredentials),
            string(name: 'awsRegion', value: params.awsRegion)
          ],
          wait: false,
          propagate: false
      }

      when {
        expression {
          return params.isGoingToDeploy
        }
      }
    }
  }
}