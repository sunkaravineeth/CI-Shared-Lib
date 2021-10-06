

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
      
      def uploadSpec = """{
     "files": [
      {
          "pattern": "**/${ARTIFACTID}.war",
          "target": "game-of-life-SNAPSHOT/"
        }
     ]
     }"""
    }
    
    stages{
      stage("Initialization"){
        steps{
          script{
            echo "---checking tools versions-------"
            sh "java -version"
            sh "mvn -v"
            def pom = readFile(file: "gameoflife-web/pom.xml")
            
          }
        }
      }
      
      stage("unit test"){
        steps{
          echo "==unit test=="
          sh "mvn clean test"
        }
      }
      
      stage("build"){
        steps{
          echo "==package=="
          sh "mvn -DskipTests package"
        }
      }
      
      stage("artifact upload"){
        steps{
          echo 'Publish the artifacts..'
          script{
                        def server = Artifactory.newServer('http://20.83.33.195:8082/artifactory', 'admin', 'Vineethraj7@')
                        server.bypassProxy = true
                        server.upload(uploadSpec)
                        echo 'Uploaded the file to Jfrog Artifactory successfully'
          }              
        }
      }
    }
    
    post{
      always{
        junit '**/target/surefire-reports/*.xml'
        archiveArtifacts artifacts: '**/target/${ARTIFACTID}.war', allowEmptyArchive: true
      }
    }
  }
  
}
