def ENVIRONMENT_MAP = [
  'Development' = [ suffix: 'dev', branch: '*/develop' ]
  'Production' = [ suffix: 'prod', branch: '*/main' ]
]

pipeline {
  agent any

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '', numToKeepStr: '5')
    disableConcurrentBuilds()
  }

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

  environment {
    environment = ENVIRONMENT_MAP[params.environment]
  }

  stages {
    stage('Checkout SCM') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: environment.branch]],
          extensions: [[$class: 'CleanCheckout']],
          doGenerateSubmoduleConfigurations: false,
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: params.sshKey, url: GIT_URL]]
        ])
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
        sh "docker build -t ${params.appRegistry}:latest-${env.environment.suffix} ."
      }
    }

    stage('Testing project') {
      steps {
        sh './vendor/bin/phpunit'
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