/* NOTE: For the bootcamp I decided to put all projects of the same module into one repo in order 
 *      to keep similar things together and to not overcrowd my github account. Thats whyy some steps
 *      require  something like 'cd 01-java-maven-app' before the actual command is executed.
 *       
 *      In a real project the Jenkinsfile and Dockerfile would be at root level of the repo
 *      so that this would not be necessary.
 */


def buildJar() {
    echo 'Building the application...'
    sh 'cd 01-java-maven-app && mvn package'
}

def buildImage() {
    echo 'Building the application...'
    withCredentials([usernamePassword(credentialsId: 'docker.hub', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh 'cd 01-java-maven-app && docker build -t aarontimberg/demo-app:jma-2.0 .'
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh 'docker push aarontimberg/demo-app:jma-2.0'
    }
}

def deployApp(){
    echo 'Deploying docker image...'
}

return this