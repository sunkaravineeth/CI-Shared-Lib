import com.GitUtil

public Map config

def call( Map config) {
  
  print currentBuild.getBuildCauses().toString()
  if (currentBuild.getBuildCauses().toString().contains('BranchIndexingCause')) {
  print "INFO: Build skipped due to trigger being Branch Indexing"
  currentBuild.result = 'ABORTED' // optves a better hint to the user tha's been skipped, rather than the default which shows it's successful
  return
  }
  
  this.configuration = configuration
  
  
  
}
