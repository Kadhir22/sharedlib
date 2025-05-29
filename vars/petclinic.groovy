pipeline {
    agent { label 'cicd' }

    stages {
        stage('Cloning frontend') {
            steps {
                dir('petclinic-spring-petclinic-angular') {
                    git branch: 'master', credentialsId: 'githu-cred', url: 'https://github.com/Kadhir22/petclinic-spring-petclinic-angular.git'
                }
            }
        }

        stage('Containerization frontend') {
            steps {
                dir('petclinic-spring-petclinic-angular') {
                    withCredentials([usernamePassword(
                        credentialsId: 'dochub-cred',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh 'docker build -t frontend:4 .'
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        sh 'docker tag frontend:4 kadhir22/angular-petclinic:4'
                        sh 'docker push kadhir22/angular-petclinic:4'
                        sh 'docker rmi kadhir22/angular-petclinic:4'
                    }
                }
            }
        }

        stage('Cleanup Workspace frontend') {
            steps {
                cleanWs()
            }
        }

        stage('Cloning backend') {
            steps {
                dir('spring-petclinic-rest') {
                    git branch: 'master', credentialsId: 'githu-cred', url: 'https://github.com/Kadhir22/spring-petclinic-rest.git'
                }
            }
        }

        stage('Containerization backend') {
            steps {
                dir('spring-petclinic-rest') {
                    withCredentials([usernamePassword(
                        credentialsId: 'dochub-cred',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh 'docker build -t backend:4 .'
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        sh 'docker tag backend:4 kadhir22/petclinic-minikube:4'
                        sh 'docker push kadhir22/petclinic-minikube:4'
                        sh 'docker rmi kadhir22/petclinic-minikube:4'
                    }
                }
            }
        }

        stage('Cleanup Workspace backend') {
            steps {
                cleanWs()
            }
        }

        stage('K8s Deployment') {
            steps {
                sh 'kubectl rollout restart deployment angular-frontend'
                sh 'kubectl rollout restart deployment petclinic-backend'
            }
        }
    }
}
