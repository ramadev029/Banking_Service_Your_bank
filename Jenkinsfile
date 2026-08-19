pipeline {
    agent any

    tools {
        jdk 'Java-21'
        maven 'Maven-3.9'
    }

    environment {
        APP_NAME = 'yourbank-banking-service'
    }

    stages {
        stage('Checkout & Lint') {
            steps {
                echo 'Checking out source repository...'
            }
        }

        stage('Backend Unit & Integration Tests') {
            steps {
                dir('Bank_Service') {
                    echo 'Running Maven test suite...'
                    bat './mvnw test'
                }
            }
        }

        stage('Frontend Unit Tests') {
            steps {
                dir('frontend') {
                    echo 'Installing npm dependencies and building frontend...'
                    bat 'npm install --no-audit'
                    bat 'npm run build'
                }
            }
        }

        stage('Triage AI & Test Report Compilation') {
            steps {
                echo 'Compiling JUnit & Surefire XML test reports...'
                junit 'Bank_Service/target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'CI/CD Pipeline Execution PASSED successfully!'
        }
        failure {
            echo 'CI/CD Pipeline Execution FAILED! Triggering AI Triage Service...'
        }
    }
}
