# 08 - Build Automation & CI/CD

## 1 - Intro to Build Automation

### What we did before

1. Built several applications locally
2. Build Docker images
3. Test functionality manually on the local machine
4. Push them to Nexus
5. Pull from a **PROD**  server

**→ Build Automation** takes over after checking in code changes to a git repository



### With Build Automation

* No switch between **DEV**, **TEST** and** ** environment during testing

  → A dedicated machine builds the app and tests it

  → All tools and configurations are already available



**Configure Jenkins**

Jenkins is a configurable CI/CD tool with plugins to use and connect to several 3rd party technologies (e.g. Docker, Git, AWS, deployment servers, etc.).

* Build the app → Build tools need to be available (npm, Gradle, mvn, docker)
* Test the app → Test environment (e.g., a test database)
* Credential management → Jenkins provide storage mechanisms to connect to Docker repositories as Nexus, etc.

→ Setup done once and can be used for multiple projects

***

## 2 - Install Jenkins

1. Set up a new remote server
   > In the bootcamp DigitalOcean was used. However since their prices are high compared to other IaaS providers I chose Strato instead.
2. Set up a firewall to allow connection on port 22 (ssh) and 8080 (Jenkins)
3. Install Docker with `apt update && apt install docker.io`
4. Start Jenkins with docker
   ```shellscript
   docker run -p 8080:8080 -p 50000:50000 -d -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
   ```
5. The url `<server-ip>:8080` opens the web UI:

   ![](assets/notes/aK_7xu4cOgf3NqMRnd7YkJ4zHQrBrQcf-dBZcbcNtWA=.png)
6. The one-time password can be found in the file which path is displayed in the UI. To access it get into the container:
   ```shellscript
   # Get the container ID using 'docker ps'
   docker exec -it ea37a451b5d8 bash

   # Output OTP with
   cat /var/jenkins_home/secrets/initialAdminPassword
   ```
7. Install recommended plugins
8. Create an admin user

***

## 4 - Install Build Tools in Jenkins

### Two ways to install build tools

1. Jenkins Plugins → via UI
   * Input fields for easy use
   * Usage limited to Jenkins UI input fields
2. Install tools directly on the server → Within the docker container that is running Jenkins
   * More effort to install
   * More flexible later on in usage



### Install maven via UI

1. If a tool is not already available in **Jenkins verwalten → Tools** install it as a **Plugin**
2. Go to **Jenkins verwalten → Tools**
3. Scroll down to **Maven Installationen**
4. Click **Maven hinzufügen** and enter a name and a version
   1. Alternatively by clicking **Installationsverfahren hinzufügen** the tool can be installed from UI via command line
5. Click **Save**

![](assets/notes/QnwMA2V5399FvDWmt9ZQkcEksEkiuAqUMmBIKbVrBgY=.png)

**→ This also works for Gradle, Ant, Git or JDKs**



### Install Node.js and npm on the server (inside Docker)

1. `ssh` to the server
2. Enter the container as a root user 
   ```shellscript
   docker exec -u 0 -it ea37a451b5d8 bash
   ```
3. Lookup the distribution (to choose to correct package manager)
   ```shellscript
   cat /etc/os-release
   ```
   → Since it is Debian **apt** is the package manager to use
4. Install curl with `apt update && apt install curl -y`
5. The following command downloads a script which puts all the npm and Node.js repos into the ***/etc/apt/sources.list*** file
   ```shellscript
   curl -sL https://deb.nodesource.com/setup_20.x -o nodesource_setup.sh
   ```
6. Execute the downloaded script
   ```shellscript
   bash nodesource_setup.sh
   ```
7. Now Node.js can be installed with `apt install nodejs -y`



### Stage View Plugin

* Plugin to show progress of stages in a pipeline

![](assets/notes/6h1az5KuNucVbfvA_rdNSIo9AsCdAhHu34D_NOXq5Yk=.png)

**Installation**

* Go to **Jenkins verwalten → Plugins → Available plugins**
* Search for *Stage View* and click **Install**
* Check the tick to restart Jenkins after installation
* Go to the server and restart the container

***

## 5 - Jenkins Basic Demo - Freestyle Job

### Create an example Freestyle Job

* Go to **Create a Job → "Free Style"-Softwareprojekt bauen** and call it **my-job**
* Scroll down to **Build Steps** and click **Build-Schritt hinzufügen**
* Select **Shell ausführen** 
  * Enter `npm --version` as an example command to execute in the input field
  > 💡Since **mvn** was installed as a plugin and not directly on the server via cmd, it is not possible to execute mvn commands with the **Shell ausführen** build step!
* Maven can be used with the **Maven Goals aufrufen** build step instead
  * Enter any mvn command like `--version`

The **Build Steps** look like this now and can be saved:

![](assets/notes/iFtQKA_BUgPKyIJs5rzdsFZxXRKdi2cVstifZlrKT4M=.png)



### Jobs View

* Now on the home page **my-job** is listed
* Clicking on that jobs shows an overview of available options and the status of the last builds

  ![](assets/notes/k8S61MTXEew7ai7L4XbhL-I5uJP2ipSbW6TGmGs3qPY=.png)
* By clicking on a build itself its console output can be seen to investigate issues in case some occur

  ![](assets/notes/6W0lTpoIcap2UESzlwCTIe7BcJXuq3MkrfOtprBLB3M=.png)



### Jobs with Git repos

* The configure part of a job provides the option to link repos

  ![](assets/notes/UKc0Ij6Y0LDtc_xoF6QbyenKhV6fLxoJAEpKtgsI6GQ=.png)
  * For authentication with GitHub the credentials of the repo have to be managed
    1. Click **Add**
    2. **Globale Zugangsdaten → Benutzername und Passwort**
    3. Fill in username. As a password use a personal access token for GitHub
  * Select a branch
  * **Save**



**The pipeline now checks out the code locally**

![](assets/notes/5iTiu2BPCcXkG7ksUzw15XLOAxr_dBNgkTzBZ_5T8qI=.png)

   → Can be used in pipelines to build apps from the code, etc.

> 💡On the jenkins server the checked out repos can be found under ***/var/jenkins\_home/workspace***.



**Execute scripts from repo** 

* In case the repo contains a script in can be executed using a **Shell ausführen** build step

  ![](assets/notes/vp53V1S_x-9pptjt26K7P4A4i9FgRf8uy2k1mMtr1DQ=.png)

→ As seen in the picture the permissions for execution have to be set before

***

## 6 - Docker in Jenkins

## Make Docker available in Jenkins

* Jenkins itself is running as a Docker container right now
* To make Docker Cli available within Jenkins the Docker socket has to be mounted into the container:
  ```shellscript
  docker run -p 8080:8080 -p 50000:50000 -d -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts
  ```
* Then, enter the container as root and run the following curl command to download and run an installer script:
  ```shellscript
  curl https://get.docker.com/ > dockerinstall && chmod 777 dockerinstall && ./dockerinstall
  ```
* Finally set the correct permissions on the ***/var/run/docker.sock*** file **inside the container**
  ```shellscript
  chmod 666 /var/run/docker.sock
  ```



### Build a Docker image using Jenkins

> **Note:** Since I store the distinct repos of a lecture in subfolders using the maven Jenkins plugin does not work for me, because it is only capable to build apps in the root folder of a repo.
> So I installed maven with `apt install mvn` directly within the jenkins container.

* First the in the first stage of the **Freestyle Job** I packaged the maven app with the following shell commands:

  ![](assets/notes/4rRwtIh1-qpSHN0bnCQIbwQHvWvBBsQTx4zGA9lAl1E=.png)
* The following Docker commands
  * build the image and tags it with the name of my repo
  * logs in to hub.docker.com using credentials that were configured in Jenkins and stored as env variables using the **Environment → Use secret text(s) or file(s)** option

    ![](assets/notes/J3WvbV5KYIn92kzLe10DSCnlZmkNwrZ_3J0uL_oB3_k=.png)
  * pushes the Docker image to hub.docker.com 



After executing the pipeline the app can be seen in the in my private repo https://hub.docker.com/repository/docker/aarontimberg/demo-app/general:

![](assets/notes/Cifds4ExJQQUM9PJS1sy1rhtwUMOSg2T53XN51XVouo=.png)



> 💡**Docker Best Practice:** \
> Provide docker credentials with stdin. So instead of \
> `docker login -u $USER -p $PASS` \
> use \
> `echo $PASS | docker login -u $USER --password-stdin`.

### Push to Nexus repository

* To push to Nexus3 from Jenkins the **insecure registries** setting has to be done on the Jenkins server in ***/etc/docker/daemon.json*** as well:
  ```json
  {
    "insecure-registries": [
      "165.245.251.67:8082"
    ]
  }
  ```
* In Jenkins create credentials for the Nexus3 server
* Rewrite the **Execute Shell**  step so that the app is tagged properly and docker logs in to Nexus
  ```shellscript
  cd 01-java-maven-app
  docker build -t 165.245.251.67:8082/java-maven-app:1.0.1 .
  echo $PASS | docker login -u $USER --password-stdin 165.245.251.67:8082
  docker push 165.245.251.67:8082/java-maven-app:1.0.1
  ```
* Make sure that
  1. the Nexus Docker container is started with the correct port bindings (8081 and 8082)
  2. and the firewall rules for both servers are configured so that they can reach each other.

***

## 8 - Intro to Pipeline Job

### Create a Pipeline job

1. **Element anlegen → Pipeline**.
2. In configuration page: **Pipeline → Pipeline script from SCM → Git** and enter repo url and select GitHub credentials.
3. Enter the **branch** you want to execute the pipeline for. Also enter the **path of the Jenkinsfile**.
   > **Note:** Usually the Jenkinsfile is in the root directory of a repo. Since I manage all projects of a module in a single repo I have to enter the actual path here.



### Jenkinsfile

* Definition of pipeline steps
* Written in **Groovy → Turing complete programming language**
* **IaS best practice:** Keep the Jenkinsfile tracked in a Git repo
* Advantage of a **Groovy** written pipeline job:
  * More complex logic possible compared to Freestyle Jobs
  * Execution in parallel is possible (e. g. running multiple tests)
  * Single source → Only one file defines the entire pipeline workflow, not a UI!
  * Less maintenance → No plugin management in the UI necessary


Two ways to write a **Jenkinsfile**

1. Scripted Pipeline
   * Groovy only syntax
   * Advanced scripting capabilities → highly flexible
   * Difficult to start with
2. Declarative Pipeline
   * Easier to get started
   * Not that powerful



##### Declarative Pipeline - Example

```groovy
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
```

The Jenkinsfile above was created in a branch **example-jenkins** and the pipeline config adapted accordingly. The execution result can be seen below:

![](assets/notes/ZHx0tXbTcjjsaYXox5jyaDUIg2xyjQ9vNAjI6ENjM0M=.png)

***

## 9 - Jenkinsfile Syntax

##### Conditions

```groovy
CODE_CHANGES = getGitChanges() // Example for a function call

pipeline {
  stages{
    stage("build") {
       when { // IF condition in expression is true
            expression {
                env.BRANCH_NAME == 'dev' && CODE_CHANGES == true // Code changes is a variable defined above
            }
        }
        steps {
          // Only executed if condition is true
        }
    }
  }
}
```



##### Environment Variables

* Jenkins provides a bunch of variables by default
* They can be found here at **\<server-url>/env-vars.html**:

  http://212.227.142.58:8080/env-vars.html/
  * Usage: `env.BRANCH_NAME`
* In addition, the engineer can provide its own variables
  ```groovy
  pipeline {
    environment {
      NEW_VERSION = '1.3.0'
    }

    ...
      steps{
        echo "Building version ${NEW_VERSION} of the application..."
      }
    ...
  }
  ```
  > 💡For env variable interpretation strings **must** **be** in **double quotes (")**.



##### Credentials

* Credentials stored in Jenkins can be used inside the Jenkinsfile
* Plugin **Credentials Binding** has to be installed for this
* They can be bound to an env variable using the function `credentials(<cred-id>)`
  ```groovy
  environment {
    PROD_SERVER_CREDS = credentials('prod-server-creds')
  }
  ```



##### Local Variables

* Using `with` blocks variables (like credentials) can be defined for a single stage
  ```groovy
  ...
  steps {
    withCredentials([ // Credentials is here the same as above
      usernamePassword(credentials: 'prod-server', usernameVariable: 'USER', passwordVariable: 'PASS')
    ]) {
        echo "Using credentials ${USER} ${PASS}"
    }
  }
  ...
  ```



##### Tools

* Provide build tools (maven, Gradle, etc.) for the stages
* These tools must be provided by plugins in **Jenkins verwalten → Tools**

  ![](assets/notes/fr-kFkWT6rxLxhya2b5Olu7oJzrJxYcgk-hcb0wWfY0=.png)
  ```groovy
  pipeline { // required - must be on toplevel
      agent any // required - which worker node should run the pipeline

      tools {
          maven "maven-3.9"
      }
  ...
  ```



##### Parameters

* Multiple types of parameters, e.g.:
  * `string(name: 'VERSION', defaultValue: '', description: 'Version to deploy on PROD')`
  * `choice(name: 'VERSION', choices: ['1.0.1', '1.1.0', '2.0.0'], description: '')`
  * `booleanParam(name: 'executeTests', defaultValue: true, description: 'Executing tests?')`
  ```groovy
  pipeline {  
    parameters {
          choice(name: 'VERSION', choices: ['1.0.1', '1.1.0', '2.0.0'], description: '')
          booleanParam(name: 'executeTests', defaultValue: true, description: '')
      }
  ...
  ```
* In the stages they could be referenced like `params.executeTests`



##### External Scripts

* Jenkinsfiles are Groovy based → Complex logics possible
* Script block in Jenkinsfile:
  ```groovy
  stage("health check") {
      script{
          // Groovy logic here
      }
  }
  ```
* **Best Practice:** In case a stage has some advanced logic → Source it out to another script file
  * Load an example Groovy script:
    ```groovy
    def gv // Variable containing the groovy script
    pipeline {
      ...
        stage("init") {
              steps {
                  script { // Assigning the script to the variable declared outside of pipeline
                      gv = load "healthCheck.groovy"  // gv can be used globally now
                  }
              }
          }
          ...
            stage("health check") {
                steps{
                    script{
                        gv.checkServerHealth()  // var.functionName
                    }
                }
            }
    ...
    ```
    > 💡**Environment variables** that were defined in the Jenkinsfile will also be **accessible within Groovy scripts** by default.



##### Input Parameters für User Input

* Use case: User has to decide to which environment a new version should be deployed to
* For that an input has to be setup with `message`, `ok` and `parameters` assigned
  ```groovy
  stage("deploy") {
      input {
          message "Select environment to deploy to"
          ok "Done"
          parameters {  // This parameters in "Parameters" above
              choice(name: 'envToDeploy', choices: ['DEV', 'STAGING', 'PROD'], description: '')
          }
      }
      steps {
          ...
      }
  }
  ```
* Alternatively the input can be assigned to a global variable for use in other stages
  ```
  stage("deploy") {
      steps{
          script{
              env.envToDeploy = input message "Select an environment to deploy to", ok: "Done", parameters: [choice(name: 'envToDeploy', choices: ['DEV', 'STAGING', 'PROD'], description: '')]
              echo "Deploying to ${envToDeploy}"
          }
      }
  }
  ```



### Replay in Jenkins UI

* In case pipeline syntax has to be tested, there is a replay function in the Jenkins UI

![](assets/notes/8jxBL_AMYVqzmhUQTC_1bT5POFqSWLYdYw0NTUpOe6M=.png)

***

## 11 - Intro to Multibranch Pipeline

* Tasks can be different for each branch
  * Test **every** branch
  * Deploy only from **main** branch
  * On merge: **build → test → deploy**
* Different behavior depending on branch is desired → Multibranch Pipeline

### How to create a Multibranch Pipeline?

* **Element anlegen → Multibranch Pipeline**
* In **Configuration → Branch Sources** you can select the SCM to use and enter the repo to check out
  * Here under **Behaviors** you can **Filter by name (with regular expression)** to auto generate pipelines for all branches that matches that pattern (e.g. the following pattern would only create pipelines for feature branches)

    ![](assets/notes/4TprHntAKMpRoiUmjn8Tmd-nkg59cuZujz24M8pf9IE=.png)



### Functionality

* A Multibranch Pipeline **automatically** creates a pipeline for **every** branch created in the SCM
* Jenkins creates a pipeline and runs it for **every** branch that has a Jenkinsfile at the configured path:

  ![](assets/notes/PLnhTD5E3aM9U9LTdwxnzY20xZbBX4wF21to14k8os0=.png)
* Jenkins provides an overview over all branches that have a Jenkinsfile and matches the expression given in configuration:

  ![](assets/notes/6lKF8PhXDqdKSRbJuaHMm6VFuhGMpaCXoWwBvc61BLE=.png)

> 💡**Best Practice:** All branches should have the **same** Jenkinsfile. Inside the shared Jenkinsfile the differentiation of the branches should be made in the logic.



### Example for branch differentiation

* **Tests**  are executed **everywhere**
* **Build** and **deploy only** takes place in **main**
  * main branch → Build and Deployment are done

    ![](assets/notes/j2Win1HfeUL9lvreIvRSreXN_K5vGbJMHpI4zQRl5rQ=.png)
  * multibranch-test branch → Only test are done

    ![](assets/notes/HBpu61hiG3HoieoEVj4K8bXKrzfeRBJsyksDLBLXol4=.png)

***

## 13 - Credentials in Jenkins

### Scopes of Credentials

* **Go to:** Jenkins verwalten → Zugangsdaten → System → Globale Zugangsdaten
* Add Credentials → Benutzername und Passwort
  * Here two scopes can be seen:
  ![](assets/notes/t1JI9CROsTZ3Z38NzRF04xURMin62VfM-TNtg8ge0sM=.png)

1. Global scope
   * Accessible everywhere
   * Can be used in Jobs
2. System scope
   * Credentials are only available on Jenkins server
   * Not accessible for Jenkins jobs
   * Only for system administration → e.g. to interact with external systems
3. Job scope
   * The **Credentials** (here "Zugangsdaten") button provides a scope for credentials that should be only available to **this job**
   * **Note:** The job scoped credentials are only available in Multibranch pipelines
   ![](assets/notes/J-oERCIIN1p5Pd0W2Stoo-4g2DqRL5jpcAfJcNUD6h0=.png)

***

## 14 - Jenkins Shared Library

### Example: Application of multiple microservices

* All are Java/Maven services
* In Jenkins → Each microservice would need to be built with a single Multibranch pipeline
* Jenkinsfile would share the (partly) the same logic → unnecessary redundancy



### Jenkins Shared Library

* Extension to the pipeline → Stored in a separate repo
* Written in Groovy
* Shared Library is referenced in Jenkinsfile
* Logic can be shared across project and teams 
* Example use case:
  * For mailing setup to a company-wide Slack channel
  * Common Nexus repo



### Structure of a Shared Library 

* vars → All functions to be executed from Jenkinsfile (each function is its own Groovy script file)
* src → Utility code for the functions
* resources → External libraries and non-groovy script



##### vars/buildJar.groovy

```groovy
#!/user/bin/env groovy

def call() {
    echo 'Building the application...'
    sh 'mvn package'
}
```

* The shebang `#!/user/bin/env groovy` should always be the first line
* The script should always contain a `call()` function which is the entrypoint for a shared library function



### Make Shared Library available **globally** in Jenkins

* Jenkins verwalten → System → Global Pipeline Libraries
* Enter a branch, commit or git tag as a **Default version** → This version is used in pipelines if not stated otherwise

  ![](assets/notes/Do4kfg_MyCA9wXGNV2ntWBkkIoVJf-ypC5L82Rz85JQ=.png)



### Import Shared Library

1. Put shebang at the first line of Jenkinsfile
2. Use `@Library('08-jenkins-shared-library')` to import the library. Use the name given in Jenkins
   ```groovy
   #!/user/bin/env groovy

   // Import library 
   // @1.0 [optional] overwrites the default version set up in Jenkins UI
   @Library('08-jenkins-shared-library@1.0') 
   def gv

   pipeline {
       agent any
       ...
   ```

   > 💡In case the pipeline starts right after `@Library('08-jenkins-shared-library')` then an underscore (`_`) has to be appended to the `@Library(..)`.
3. The `buildJar()` can referenced by its file name in the shared library
   ```groovy
   stage('Build jar') {
       steps {
           script {
               dir('01-java-maven-app') {
                   buildJar() // Name of the file in the shared library
               }
           }
       }
   }
   ```



### Use parameters in shared library

##### vars/buildImage.groovy

```groovy
def call(String imageTag) {
    echo 'Building the application...'
    sh "docker build -t $imageTag ."  // Strings need to be double quoted in order to resolve the parameter
}
```

##### Usage in Jenkinsfile

```groovy
stage('Build docker image') {
  steps {
    script {
      buildImage 'aarontimberg/demo-app:jma-2.1' // Provide tag as parameter
    }
  }
}
```



### Source out Code in shared library to src

* Reusable code that can be used across multiple methods of a shared library can be created in ***src***
  * Create a package in ***src***, like ***com.example***
  * Create a groovy class inside that package, e.g. ***Docker***
* Write a `Docker` class
  ```groovy
  #!/user/bin/env groovy

  package com.example

  class Docker implements Serializable {

      def script

      Docker(script){
          this.script = script
      }

      def buildDockerImage(String imageTag) {
          script.echo 'Building the application...'
          script.withCredentials([script.usernamePassword(credentialsId: 'docker.hub', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
              script.sh "docker build -t $imageTag ."
              script.sh "echo '${script.PASS}' | docker login -u '{$script.USER}' --password-stdin"
              script.sh "docker push $imageTag"
          }
      }
  }

  ```
  * All classes used in Jenkins pipelines **must** implement `Serializable`
  * The `script` parameter **must** be passed to a class in order to use Jenkins **pipeline** **environment**
* Now this class can be used in ***vars/buildImage*** or in any other method by importing it
  ```groovy
  #!/user/bin/env groovy

  import com.example.Docker

  def call(String imageTag) {
      return new Docker(this).buildDockerImage(imageTag) // Passing context 'this' to Docker Descriptor
  }
  ```
* Overview of directory structure:

  ![](assets/notes/pFK2muve8QysSC34SS2fFwPT26BreHdxtnGGVt4XGKM=.png)

> 💡**Best practice:** The code in ***src*** could be directly imported in the Jenkinsfile. However, it is a best practice to **only use the methods** from ***vars*** inside the Jenkinsfile and not to import from ***src*** directly!



### Make shared library available **locally**

* Shared Libraries that should only be accessible within a single repo can be imported directly in the Jenkinsfile
  ```groovy
  library identifier: "08-jenkins-shared-library@main", retriever: modernSCM([
      $class: 'GitSCMSource',
      remote: 'https://github.com/timmartinberger/08-jenkins-shared-library.git',
      credentialsId: '97fe2288-6e06-4306-99ac-4bd45c00ae7a'])
  ```

***

## 15 - Webhooks - Trigger Pipeline Jobs automatically

### Jenkins configuration for auto triggering **regular pipeline**

##### Jenkins part

* Install plugin: **GitHub Plugin**
* In: Jenkins verwalten → System → GitHub
  * Add the GitHub repo server 
  * Add credentials as a **Secret text** → Add the GitHub token with webhook access here

    ![](assets/notes/lOzsW0lWp_r_gLBWVDiUyRy2K1EbKxg9qXwBtWvNAGI=.png)
* In a classic pipeline you can find a section **Triggers** now
* Check the option as in the image below to activate autotriggering

  ![](assets/notes/dEsEakuefoig78Ns3rKj4LKgI5mMpYrzs-FNb8v5pOU=.png)



##### GitHub part

* Go to **your repo** and then **Settings → Webhooks**  and click **Add webhook**
* Choose the options as in the image below to push changes in the repo to the Jenkins server (SSL needs to be disabled here)

  ![](assets/notes/uMXPHsv2z9f1eQwPh4Ykn82iekX1EFV_Xi7Yb4F8rFQ=.png)



### Jenkins configuration for auto triggering multibranch pipeline

##### Jenkins part

* Install plugin: **Multibranch Scan Webhook Trigger**
* Go to the Multibranch pipeline Job → Configure
* Check **Scan by webhook**  and enter any arbitrary token

![](assets/notes/7pZbc3F3bHMqvl2XNSJGykoFgQhIc1JFJFp0JfWzZJ4=.png)

##### GitHub part

* Create a webhook with the url  

  `JENKINS_URL/multibranch-webhook-trigger/invoke?token=[Trigger token]`

  ![](assets/notes/SqmRYw3usx6o8yj3bNgFdFLaL4_x7o9wB6pbVOzd6m8=.png)

***

## 16 - Dynamically Increment Application version in Jenkins Pipeline - Part 1

* Versioning is managed by package managers (e.g. maven, Gradle, etc.) 

  → Build tools provide commands to increase version
* Version are increased in the following pattern:
  * `4.1.2-SNAPSHOT` → `<major>.<minor>.<patch>-suffix`
  * Major version → Big / Breaking changes that are **not** backward-compatible
  * Minor version → New features, but backward-compatible
  * Patch version → Bugfixes, no new features or API changes
  * Suffix → Adds additional information

> 💡 Sometimes there is something like `1.1.0.17` where the fourth version number is the **build number**.



### Automatic versioning with maven

* Maven plugin for parsing version `build-helper` → `parse-version` function parses the ***pom.xml** file and splits the version internally into parts
* `version:set` is used to set the new version based on input from `build-helper:parse-version`
  ```shellscript
  # Increase the patch / incremental version
  mvn build-helper:parse-version versions:set \
    -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} \
    versions:commit # <-- This overwrites the version in pom.xml
  ```
  > To access variables in a shell: Windows → `${var}` | Linux → `\${var}`
  * parse-version provides the following variables:
    * `majorVersion` and `newMajorVersion`
    * `minorVersion` and `newMinorVersion`
    * `incrementalVersion` and `newIncrementalVersion`
  * The corresponding version to increment can be set using the `-DnewVersion` parameter as in the command above



### Integration in a CI/CD pipeline

* Each commit to main should be a new version
* A new stage is added **before** building the app and the docker image
  ```groovy
  stage('Increment version') {
      steps {
          script {
              echo "Incrementing app version"
              sh 'mvn build-helper:parse-version versions:set \
                  -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
                  versions:commit'
              def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
              def version = matcher[0][1] // matcher[0][1] is the actual version inside the pom.xml
              env.IMAGE_NAME = "$version.$BUILD_NUMBER"
          }
      }
  }
  ```
  1. The version in the ***pom.xml*** is increased as shown above
  2. Then, the version is red from the pom.xml
  3. The `BUILD_NUMBER` environment variable is the number of the Jenkins pipeline run

     → `BUILD_NUMBER` is appended to the jar file version to have a unique version for each docker file build



##### Adaptation of Dockerfile

* When copying the jar file then the file name should not be hard coded
  ```docker
  # DO NOT use this
  COPY ./target/java-maven-app-1.1.0-SNAPSHOT.jar /usr/app/
  CMD java -jar java-maven-app-1.1.0-SNAPSHOT.jar

  # DO this instead
  COPY ./target/java-maven-app-*.jar /usr/app/
  CMD java -jar java-maven-app-*.jar
  ```

  ##### Make sure to clean up old builds
  ```groovy
  def buildJar() {
      echo 'Building the application...'
      sh 'mvn clean package'
  }
  ```

***

## 17 - Dynamically Increment Application version in Jenkins Pipeline - Part 2

* New version of the app is never pushed back to the source code in the repo

  → Jenkins always starts from the same version

  **→ New version has to be commited back to the repo**



### Push new version from Jenkins to repo

* **Important:** The git user and email on Jenkins has to be configured in order to push to the repo
  * This has to be done only once and can be removed after the first execution

```groovy
stage('Commit version update') {
    steps {
        script {
            withCredentials([usernamePassword(credentialsId: '97fe2288-6e06-4306-99ac-4bd45c00ae7a', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                // This only has to be done once (can also be done inside the jenkins container)
                sh 'git config --global user.email "jenkins@example.com"'
                sh 'git config --global user.name "jenkins"'

                sh "git remote set-url origin https://${USER}:${PASS}@github.com/timmartinberger/devops-bootcamp-08-jenkins.git"
                sh 'git add .'
                sh 'git commit -m "ci: version bump"'
                sh 'git push origin HEAD:main'
            }
        }
    }
}
```



##### Commit Loop

* Jenkins goes in an infinite loop:

  Jenkins checks out changes → Builds app → pushes new version number to repo → Jenkins checks out changes → ...
* Avoid this loop in pipeline config:
  1. Install **Ignore Committer Strategy** plugin
  2. Under **Configure → Branch Sources** add a **Build Strategy**
  3. Select **Ignore Commiter Strategy** and enter the email configured for Jenkins
  ![](assets/notes/8s-P9p3PCSBFojf0OsFAex0N-akmCeS0mwJSLTgD4KY=.png)
