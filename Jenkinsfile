def gv

pipeline { // required - must be on toplevel
    agent any // required - which worker node should run the pipeline

    parameters {
        booleanParam(name: 'executeTests', defaultValue: true, description: '')
    }


    tools {
        maven "maven-3.9"
    }

    environment {
        NEW_VERSION = '1.3.0'
    }

    stages { // required - where the work happens
        stage("init") {
            steps {
                script {
                    gv = load "healthCheck.groovy"
                }
            }
        }
        stage("build") {
            when {
                expression {
                    env.BRANCH_NAME == 'dev' // Built-in variable of Jenkins
                }
            }
            steps {
                echo "Building version ${NEW_VERSION} of the application..."
                sh 'mvn install'
            }
        }
        stage("test") {
            when { // Basic condition
                expression { // This stage is only executed if params.executeTests is true
                    params.executeTests
                }
            }
            steps {
                echo 'Testing the application...'
            }
        }
        stage("deploy variant 1") {
            input { // User input in pipeline
                message "Select environment to deploy to"
                ok "Done"
                parameters {
                    choice(name: 'envToDeploy', choices: ['DEV', 'STAGING', 'PROD'], description: '')
                }
            }
            steps {
                withCredentials([ // Usage of credentials that were setup within Jenkins
                    usernamePassword(credentialsId: 'prod-server', usernameVariable: 'USER', passwordVariable: 'PASS')
                ]) {
                    echo "[Variant 1] Using credentials ${USER} ${PASS}"
                    echo "[Variant 1] Deploying to ${envToDeploy}"
                }
            }
        }
        stage("deploy variant 2") {
            steps{
                script{
                    env.envDeploy = input message: "Select an environment to deploy to", ok: "Done", parameters: [choice(name: 'envToDeploy', choices: ['DEV', 'STAGING', 'PROD'], description: '')]
                    echo "[Variant 2] Deploying to ${envDeploy}"
                }
            }
        }
        stage("health check") {
            steps{
                script{
                    gv.checkServerHealth()
                }
            }
        }
    }

    post { // Executes logic after all stages base on condition
        always {
            // Whether build failed or succeeded
            echo 'Mailing results to DevOps team...'
            // ...
        }
        success {
            // Only executed in case of success
            echo 'Pipeline execution successful'
        }
        failure {
            // Only executedin case of failure
            echo 'At least one step failed'
        }
    }
}
