def gitCloneURL = 'git@github.com:itorix/apiwiz-release-pipeline.git'
def gitBranch = 'prod'
def gitCred =  'github-apiwiz'
def commonFunctions

node {
  git branch: gitBranch, credentialsId: gitCred, poll: false, url: gitCloneURL
  commonFunctions = load 'appjenkinsfile/JenkinsfileV2.groovy'
}

commonFunctions.runjenkinsfile()
