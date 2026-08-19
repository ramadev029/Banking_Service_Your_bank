pipeline {
    agent any

    triggers {
        githubPush()
        pollSCM('H/2 * * * *')
    }

    environment {
        APP_NAME = 'yourbank-banking-service'
        TRIAGE_API_URL = 'http://localhost:8085/api/v1/triage/analyze'
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
                    echo 'Executing automated test suite via Maven Wrapper...'
                    bat 'call mvnw.cmd test -Dtest=VerhoeffAlgorithmTest,TransactionServiceUnitTest,TriageClassifierUnitTest -Dmaven.test.failure.ignore=true'
                }
            }
        }

        stage('Frontend Build & Verification') {
            steps {
                dir('frontend') {
                    echo 'Installing npm dependencies and building frontend...'
                    bat 'call npm.cmd install --no-audit'
                    bat 'call npm.cmd run build'
                }
            }
        }

        stage('AI Triage Ingestion & Test Report Compilation') {
            steps {
                echo 'Compiling JUnit XML test reports for AI Triage Analysis...'
                junit allowEmptyResults: true, testResults: 'Bank_Service/target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            echo 'Sending test run metrics & failure logs to AI Triage Assistant Backend...'
            bat 'powershell -Command "$files = Get-ChildItem -Path Bank_Service/target/surefire-reports/*.xml; foreach($f in $files){ $xml = Get-Content $f.FullName -Raw; $json = @{suiteName=\'Jenkins CI/CD Build\'; xmlContent=$xml} | ConvertTo-Json; Invoke-RestMethod -Uri \'%TRIAGE_API_URL%\' -Method Post -ContentType \'application/json\' -Body $json }"'
            cleanWs()
        }
        success {
            echo '✅ CI/CD Pipeline Execution PASSED successfully!'
        }
        failure {
            echo '🚨 CI/CD Build Failure Detected! Dispatched failure logs to AI Triage Classifier for 4-Way Failure Analysis & Jira Defect Drafting.'
        }
    }
}
