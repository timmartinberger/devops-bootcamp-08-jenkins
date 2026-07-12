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

def return this