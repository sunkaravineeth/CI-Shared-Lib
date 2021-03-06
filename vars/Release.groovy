
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
      def downloadSpec = """{
     "files": [
      {
          "pattern": "game-of-life-SNAPSHOT/",
          "target": "/var/lib/jenkins/workspace/Release_master/"
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
            sh "git tag -l > git-tags.txt"
            git_tags = readFile "git-tags.txt"
            timeout(time: 5, unit: 'MINUTES'){
              chooseRef = input message: "version to deploy", ok: "proceed", parameters: [
                choice(choices: git_tags, name: 'Git tag', description: 'select git tag for deployment'),
                string(
                  description: 'this is going to override the default tag',
                  name: 'Branch name'
                  )
                ]
            }
            
            sh "git checkout ${chooseRef['Git tag']}"
            echo "value: ${chooseRef['Git tag']}"
          }
        }
      }
      
      stage("read type of deployment"){
        steps{
          script{
            dply_type=['install','update']
            timeout(time: 5, unit: 'MINUTES'){
              chooseRef = input message: "Type of Deployment", ok: "proceed", 
              parameters: [
                choice(choices: dply_type, name: 'Deployment Type', description: 'select type of deployment')
              ]
            }
          }
        }
      }
      
      stage("download artifacts"){
        steps{
          script
                        {
                        def server = Artifactory.newServer('http://20.83.33.195:8082/artifactory', 'admin', 'Vineethraj7@')
                        server.bypassProxy = true
                        server.download(downloadSpec)
                        echo 'Downloaded the file from Jfrog Artifactory successfully'
                        }
        }
      }
    }
  }
}

