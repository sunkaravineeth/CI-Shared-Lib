

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
    
    stages{
      stage("Initialization"){
        steps{
          script{
            echo "---checking tools versions-------"
            sh "java -version"
            sh "mvn -v"
            def pom = readFile(file: "gameoflife-web/pom.xml")
            echo " version: $pom.version "
          }
        }
      }
    }
     
  }
  
}
