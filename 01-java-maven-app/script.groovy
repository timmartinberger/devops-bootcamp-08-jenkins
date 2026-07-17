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

def deployApp(){
    echo 'Deploying docker image...'
}

return this