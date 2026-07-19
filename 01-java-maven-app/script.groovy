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

def mvnVersioning() {
    echo "Incrementing app version..."
    sh 'mvn build-helper:parse-version versions:set \
        -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
        versions:commit'
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    def version = matcher[0][1] // matcher[0][1] is the actual version inside the pom.xml
    env.IMAGE_NAME = "jma-$version.$BUILD_NUMBER"
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

def gitCommit(){
    withCredentials([usernamePassword(credentialsId: '97fe2288-6e06-4306-99ac-4bd45c00ae7a', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh "git remote set-url origin https://${USER}:${PASS}@github.com/timmartinberger/devops-bootcamp-08-jenkins.git"
        sh 'git add .'
        sh 'git commit -m "ci: version bump"'
        sh 'git push origin HEAD:main'
    }
}

return this