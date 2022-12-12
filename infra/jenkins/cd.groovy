def COLOR_MAP = [
    'SUCCESS': 'success',
    'FAILURE': 'failure',
]

Map ENVIRONMENT_MAP = [
  'Development': [ suffix: 'dev', branch: '*/develop' ],
  'Production': [ suffix: 'prod', branch: '*/main' ],
]

pipeline {
  agent any

  parameters {
    string(name: 'sshKey', defaultValue: '')
    string(name: 'projectName', defaultValue: '')
    string(name: 'environmentParam', defaultValue: '')
    string(name: 'appRegistryUrl', defaultValue: '')
    string(name: 'ecrRegistryCredential', defaultValue: '')
    string(name: 'awsCredentials', defaultValue: '')
    string(name: 'awsRegion', defaultValue: '')
  }

  environment {
    environment = ENVIRONMENT_MAP[params.environment]
    clusterName = params.projectName + '-CLU-' + environment.suffix
    serviceName = params.projectName + '-SRV-' + environment.suffix
  }

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '', numToKeepStr: '5')
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout SCM') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: env.environment.branch]],
          extensions: [[$class: 'CleanCheckout']],
          doGenerateSubmoduleConfigurations: false,
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: params.sshKey, url: GIT_URL]]
        ])
      }
    }

    /*stage('Versioning project') {
      steps {
        sh "docker tag ${params.appRegistry}:${env.environment.suffix}-latest ${params.appRegistry}:V-${env.environment.suffix}-${env.BUILD_ID}"
      }
    }

    stage('Publishing project to ECR') {
      steps {
        docker.withRegistry(appRegistryUrl, ecrRegistryCredential) {
          sh "docker push ${params.appRegistry}:V-${env.environment.suffix}-${env.BUILD_ID}"
          sh "docker push ${params.appRegistry}:${env.environment.suffix}-latest"
        }
      }
    }

    stage('Deploy app to ECS') {
      steps {
        withAWS(credentials: awsCredentials, region: awsRegion) {
          sh "aws ecs update-service --cluster ${env.clusterName} --service ${env.serviceName} --force-new-deployment"
        }
      }
    }*/

    stage('Debug') {
      echo 'It is working as well :)'
    }
  }

  /*post {
    always {
      echo 'Slack notifications.'
      slackSend channel: '#dev',
        color: COLOR_MAP[currentBuild.currentResult],
        message: "*${currentBuild.currentResult}:* Job: ${env.JOB_NAME} build: ${env.BUILD_ID}\nMore info at: ${env.BUILD_URL}"
    }
  }*/
}