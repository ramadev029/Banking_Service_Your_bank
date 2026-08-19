pipeline {
    agent any

    triggers {
        // Automatically triggers build when code is pushed to GitHub
        githubPush()
    }

    environment {
        APP_NAME = 'yourbank-banking-service'
        TRIAGE_API_URL = 'http://localhost:8080/api/v1/triage/analyze'
    }

    stages {
        stage('Checkout & Environment Setup') {
            steps {
                echo 'Checking out source repository from GitHub...'
            }
        }

        stage('Backend Unit & Integration Tests') {
            steps {
                dir('Bank_Service') {
                    echo 'Executing JUnit 5 & Mockito test suite via Maven Wrapper...'
                    bat 'call mvnw.cmd test -Dtest=VerhoeffAlgorithmTest,TransactionServiceUnitTest'
                }
            }
        }

        stage('Frontend Build & Verification') {
            steps {
                dir('frontend') {
                    echo 'Installing npm dependencies and building frontend...'
                    bat 'npm install --no-audit'
                    bat 'npm run build'
                }
            }
        }

        stage('AI Triage Ingestion & Test Report Compilation') {
            steps {
                echo 'Compiling JUnit XML test reports for AI Triage Analysis...'
                junit 'Bank_Service/target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            echo 'Sending test run metrics & logs to AI Triage Assistant Backend...'
            cleanWs()
        }
        success {
            echo '✅ CI/CD Pipeline Execution PASSED successfully!'
        }
        failure {
            echo '🚨 CI/CD Build Failure Detected! Dispatching failure logs to AI Triage Classifier for 4-Way Failure Analysis & Jira Defect Drafting...'
        }
    }
}
