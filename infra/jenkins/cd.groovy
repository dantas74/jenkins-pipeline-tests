def COLOR_MAP = [
    'SUCCESS': 'success',
    'FAILURE': 'failure',
]

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
    string(name: 'sshKey', defaultValue: '')
    string(name: 'projectName', defaultValue: '')
    string(name: 'environmentParam', defaultValue: '')
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
          env.branchName = ENVIRONMENT_BRANCH_MAP[params.environmentParam]
          env.suffix = ENVIRONMENT_SUFFIX_MAP[params.environmentParam]
          env.clusterName = params.projectName + '-CLU-' + environment.suffix
          env.serviceName = params.projectName + '-SRV-' + environment.suffix
        }
      }
    }

    stage('Checkout SCM') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: env.branchName]],
          extensions: [[$class: 'CleanCheckout']],
          doGenerateSubmoduleConfigurations: false,
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: params.sshKey, url: GIT_URL]]
        ])
      }
    }

    /*stage('Versioning project') {
      steps {
        sh "docker tag ${params.appRegistry}:${env.suffix}-latest ${params.appRegistry}:V-${env.suffix}-${env.BUILD_ID}"
      }
    }

    stage('Publishing project to ECR') {
      steps {
        docker.withRegistry(appRegistryUrl, ecrRegistryCredential) {
          sh "docker push ${params.appRegistry}:V-${env.suffix}-${env.BUILD_ID}"
          sh "docker push ${params.appRegistry}:${env.suffix}-latest"
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
      steps {
        echo 'It is working as well :)'
      }
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