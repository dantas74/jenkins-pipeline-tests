pipeline {
  agent any

  environment {
    projectName = 'proesc-backend' // Name of the folder containing the jobs
    sshKey = 'proesc-ssh-key' // Name of the credentials id to connect to git
    githubWebhookToken = 'github-token-matheus-dr' // Github webhook credential token
    appRegistry = '607751015014.dkr.ecr.sa-east-1.amazonaws.com/proesc-backend' // App registry in ECR
    appRegistryUrl = 'https://607751015014.dkr.ecr.sa-east-1.amazonaws.com' // ECR registry url
    ecrRegistryCredential = 'ecr:sa-east-1:aws-CRED' // Access way of docker plugin for AWS
    awsCredentials = 'aws-CRED' // Name of credentials id to connect to AWS
    awsRegion = 'sa-east-1' // AWS Region
  }

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '', numToKeepStr: '5')
    disableConcurrentBuilds()
  }

  triggers {
    GenericTrigger (
      genericVariables: [
        [key: 'pushRef', value: '$.ref', defaultValue: ''],
        [key: 'baseRef', value: '$.pull_request.base.ref', defaultValue: ''],
        [key: 'headRef', value: '$.pull_request.head.ref', defaultValue: ''],
        [key: 'mergeIsGoingToDeploy', value: '$.pull_request.merged', defaultValue: ''],
        [key: 'isMergeable', value: '$.pull_request.mergeable', defaultValue: ''],
        [key: 'mergeAction', value: '$.action', defaultValue: '']
      ],
      // Always change for each project
      token: 'proesc-backend'
    )
  }

  stages {
    stage('Checkout SCM') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [
            [name: '*/develop'],
            [name: '*/release'],
            [name: '*/main'],
            [name: '*/feature*'],
            [name: '*/hotfix*'],
            [name: '*/bugfix*']
          ],
          extensions: [[$class: 'CleanCheckout']],
          doGenerateSubmoduleConfigurations: false,
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: sshKey, url: GIT_URL]]
        ])
      }
    }

    stage('Setup') {
      steps {
        script {
          // Defining vars to use in script
          def mergeIsGoingToDeploy = env.mergeIsGoingToDeploy
          def baseRef = env.baseRef
          def headRef = env.headRef
          def pushRef = env.pushRef
          def action = env.mergeAction          

          // Checking if is going to deploy based on merge or push
          env.isGoingToDeploy = mergeIsGoingToDeploy ?: pushRef ==~ /^refs\/heads\/(develop|release|main).*$/

          // Validation of auxiliary branches
          env.auxiliaryCi = (
            headRef ==~ /^(feature|hotfix|bugfix).*$/ && (action == 'opened' || action == 'reopened')
          )

          // Validation of dev environment
          env.devCiCd = (
            baseRef == 'develop' && env.isGoingToDeploy.toBoolean()
          ) || (pushRef == 'refs/heads/develop') || (
            headRef ==~ /^(feature|hotfix|bugfix|release).*$/ && action == 'closed' && env.isGoingToDeploy.toBoolean()
          )

          // Validation of qa environment
          env.qaCiCd = (
            baseRef ==~ 'release' && env.isGoingToDeploy.toBoolean()
          ) || (pushRef ==~ 'refs/heads/release')

          // Validation of prod environment
          env.prodCiCd = (
            baseRef == 'main' && env.isGoingToDeploy.toBoolean()
          ) || (pushRef == 'refs/heads/main') || (
            headRef ==~ /^(hotfix|release).*$/ && action == 'closed' && env.isGoingToDeploy.toBoolean()
          )
        }
      }
    }

    stage('Auxiliary branch') {
      steps {
        build job: "${env.projectName}/ci-auxiliary",
          parameters: [
            string(name: 'branchName', value: headRef),
            string(name: 'projectName', value: projectName),
            string(name: 'sshKey', value: sshKey),
            string(name: 'githubWebhookToken', value: githubWebhookToken)
          ],
          wait: false,
          propagate: false
      }

      when {
        expression {
          return env.auxiliaryCi.toBoolean()
        }
      }
    }

    stage('Dev Environment') {
      steps {
        build job: "${env.projectName}/ci",
          parameters: [
            booleanParam(name: 'isGoingToDeploy', value: isGoingToDeploy),
            string(name: 'projectName', value: projectName),
            string(name: 'sshKey', value: sshKey),
            string(name: 'appRegistry', value: appRegistry),
            string(name: 'environmentParam', value: 'Development'),
            string(name: 'appRegistryUrl', value: appRegistryUrl),
            string(name: 'ecrRegistryCredential', value: ecrRegistryCredential),
            string(name: 'awsCredentials', value: awsCredentials),
            string(name: 'awsRegion', value: awsRegion)
          ],
          wait: false,
          propagate: false
      }

      when {
        expression {
          return env.devCiCd.toBoolean()
        }
      }
    }

    /*stage('Prod Environment') {
      steps {
        // TODO: Change job to correct pipeline and environment
        build job: "${env.projectName}/ci-dev",
          parameters: [
            booleanParam(name: 'isGoingToDeploy', value: isGoingToDeploy)
          ],
          wait: false,
          propagate: false
      }

      when {
        expression {
          return env.prodCiCd.toBoolean()
        }
      }
    }*/
  }
}