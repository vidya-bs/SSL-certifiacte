def gitCloneURL = 'git@github.com:itorix/itorix-app-jenkins-build.git'
def gitBranch = 'feature-1'
def gitCred =  'github-core'
def commonFunctions

node {
  git branch: gitBranch, credentialsId: gitCred, poll: false, url: gitCloneURL
  commonFunctions = load 'appjenkinsfile/Jenkinsfile.groovy'
}

commonFunctions.runjenkinsfile()
