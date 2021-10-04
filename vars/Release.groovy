
public Map config

def call( Map config) {
  
  print currentBuild.getBuildCauses().toString()
  if (currentBuild.getBuildCauses().toString().contains('BranchIndexingCause')) {
  print "INFO: Build skipped due to trigger being Branch Indexing"
  currentBuild.result = 'ABORTED' // optves a better hint to the user tha's been skipped, rather than the default which shows it's successful
  return
  }
  
  this.config = config
  
  pipeline{
    agent any
    
    options{
      
      timeout(
        time: config.timeoutTime ? config.timeoutTime : 6,
        unit: config.timeoutUnit ? config. timoutTUnit: "HOURS"
      )
      
      disableConcurrentBuilds()
      
      buildDiscarder(
        logRotator(
          numToKeepStr: config.logRotatorNumToKeep ? config.logRotatorNumToKeep : '30', 
          artifactNumToKeepStr: config.logRotatorArtifactNumToKeep ? config.logRotatorArtifactNumToKeep : '3'
        )
      )    
    }
    
    environment{
      ARTIFACTID = readMavenPom().getArtifactId()
      VERSION = readMavenPom().getVersion()
    }
    
    stages{
      stage("Initialization"){
        steps{
          script{
            echo "---checking tools versions-------"
            sh "java -version"
            sh "mvn -v"
            pom = readMavenPom file: "pom.xml"
            echo "artifact: ${ARTIFACTID}"
            echo "version: ${VERSION}"
            
          }
        }
      }
      
      stage("Deployment"){
        steps{
          script{
            echo "--------Deploy--------"
            def chooseRef
            sh "git tag --list --sort=refname v* > git-tags.txt"
            git_tags = readFile "git-tags.txt"
            println git_tags
          }
        }
      }
      
    }
  }
}

