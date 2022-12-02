def COLOR_MAP = [
    'SUCCESS': 'good',
    'FAILURE': 'danger',
]

pipeline {
    agent any

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '', numToKeepStr: '5')
        disableConcurrentBuilds()
    }

    /*environment {
        registryCredential = 'ecr:sa-east-1:aws-CRED'
        appRegistry = '607751015014.dkr.ecr.sa-east-1.amazonaws.com/proesc-backend'
        backendRegistryUrl = 'https://607751015014.dkr.ecr.sa-east-1.amazonaws.com'
        cluster = 'proesc-backend-CLU'
        service = 'proesc-backend-SRV'
    }*/

    stages {
        stage('Cleaning old dependencies and cache') {
            steps {
                /*sh 'composer clearcache'
                sh 'rm -rf vendor/*'*/
                echo 'Will run in develop (merge request), release and main'
            }

            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    branch 'develop'
                }
            }
        }

        stage('Installing dependencies') {
            steps {
                /*sh 'composer install'*/
                echo 'Will run in develop (merge request), release and main'
            }

            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    branch 'develop'
                }
            }
        }

        stage('Testing project') {
            steps {
                /*sh './vendor/bin/phpunit'*/
                echo 'Will run in develop (merge request), release and main'
            }

            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    branch 'develop'
                }
            }
        }

        stage('Building image with docker') {
            steps {
                /*script {
                    dockerImage = docker.build(appRegistry + ":${BUILD_NUMBER}", ".")
                }*/
                echo 'Will run in develop (merge request), release and main'
            }

            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    branch 'develop'
                }
            }
        }

        stage('Publishing project to ECR') {
            steps {
                /*script {
                    docker.withRegistry(backendRegistryUrl, registryCredential) {
                        dockerImage.push("${BUILD_NUMBER}")
                        dockerImage.push("latest")
                    }
                }*/
                echo 'Will run in develop, release and main'
            }

            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    allOf {
                        branch 'develop'
                        pullRequestReview(reviewStates: ['approved'])
                    }
                }
            }
        }

        stage('Deploy app to ECS') {
            steps {
                /*withAWS(credentials: 'aws-CRED', region: 'sa-east-1') {
                    sh 'aws ecs update-service --cluster ${cluster} --service ${service} --force-new-deployment'
                }*/
                echo 'Will run in develop, release and main'
            }

            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                    allOf {
                        branch 'develop'
                        pullRequestReview(reviewStates: ['approved'])
                    }
                }
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
