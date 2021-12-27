#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        jdk 'java17'
    }
    environment {
        MODGRADLE_CI = 'true'
    }
    stages {
        stage('Clean') {
            steps {
                echo 'Cleaning Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }

        stage('Build') {
            steps {
                echo 'Building'
                sh './gradlew build'
            }
        }

        stage('Archive artifacts') {
            steps {
                echo 'Archive'
                archiveArtifacts 'build/libs*/*jar'
            }
        }

        stage('Upload artifacts to CurseForge') {
            steps {
                echo 'Uploading to CurseForge'
                sh './gradlew curseforge'
            }
        }
        
        stage('Upload artifacts to Modrinth') {
            steps {
                echo 'Uploading to Modrinth'
                sh './gradlew modrinth'
            }
        }

        stage('Publish artifacts') {
            steps {
                echo 'Publishing'
                sh './gradlew publish'
            }
        }
    }
}