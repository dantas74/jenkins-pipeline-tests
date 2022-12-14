def ENVIRONMENT_MAP = [
    'Development': 'proesc-backend-dev-env;./infra/terraform/environments/dev;*/develop;dev',
    'Production': 'proesc-backend-prod-env;./infra/terraform/environments/prod;*/main;prod',
]

def COLOR_MAP = [
    'SUCCESS': 'success',
    'FAILURE': 'failure',
]

pipeline {
  agent any

  parameters {
    string(name: 'sshKey', defaultValue: '')
    string(name: 'projectName', defaultValue: '')
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
          def vars = ENVIRONMENT_MAP[params.environmentParam].split(';')

          env.credentialsFile = vars[0]
          env.terraformPath = vars[1]
          env.branchName = vars[2]
          env.suffix = vars[3]

          env.clusterName = "${params.projectName}-CLU-${env.suffix}"
          env.serviceName = "${params.projectName}-SRV-${env.suffix}"
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

    /*stage('Update infra') {
      steps {
        withCredentials([file(credentialsId: env.credentialsFile, variable: 'FILE')]) {
          sh 'use $FILE'
          sh "terraform -chdir=${env.terraformPath} apply -auto-approve"
        }
      }
    }

    stage('Versioning project') {
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