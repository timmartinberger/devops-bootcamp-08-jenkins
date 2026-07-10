pipeline { // required - must be on toplevel
  agent any // required - which worker node should run the pipeline

  stages { // required - where the work happens
    stage("build") {
      steps {
        echo 'Building the application...'
      }
    }
    stage("test") {
      steps {
        echo 'Testing the application...'
      }
    }
    stage("deploy") {
      steps {
        echo 'Deploying the application to PROD...'
      }
    }
  }

}
