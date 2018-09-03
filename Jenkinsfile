#!groovy

import groovy.json.JsonOutput

stage('Test') {
    node {

        checkout scm

        def build = "${env.JOB_NAME} - #${env.BUILD_NUMBER}".toString()

        def email = [to: "${env.EMAIL}", from: "${env.EMAIL}"]

        currentBuild.result = "SUCCESS"

        try {
            try {
                sh './gradlew clean build'
            } finally {
                junit "build/test-results/**/*.xml"
            }
        } catch (err) {
            currentBuild.result = "FAILURE"

            email.putAll([subject: "$build failed!", body: "${env.JOB_NAME} failed! See ${env.BUILD_URL} for details."])

            emailext body: email.body, recipientProviders: [[$class: 'DevelopersRecipientProvider']], subject: email.subject, to: "${env.EMAIL}"

            throw err
        }
    }
}
