/* NOTE: For the bootcamp I decided to put all projects of the same module into one repo in order 
 *      to keep similar things together and to not overcrowd my github account. Thats whyy some steps
 *      require  something like 'cd 01-java-maven-app' before the actual command is executed.
 *       
 *      In a real project the Jenkinsfile and Dockerfile would be at root level of the repo
 *      so that this would not be necessary.
 */

def testApp() {
    echo "Testing the application..."
}

def buildJar() {
    echo 'Building the application...'
    sh 'mvn clean package'
}

def buildImage() {
    echo 'Building the application...'
    withCredentials([usernamePassword(credentialsId: 'docker.hub', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh "docker build -t aarontimberg/demo-app:${IMAGE_NAME} ."
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh "docker push aarontimberg/demo-app:${IMAGE_NAME}"
    }
}

def deployApp(){
    echo 'Deploying docker image...'
}

return this