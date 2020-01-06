Java-Cucumber
======
Built using Java, Selenium, Gradle, Cucumber.

Web Page Object based test automation skeleton with parallel option

Resources
---
- [Git](https://git-scm.com/docs)
- [Gradle](https://docs.gradle.org/current/userguide/userguide.html)
- [Cucumber-JVM](https://cucumber.io/docs/reference/jvm)
- [Cucumber-JVM API](http://cucumber.github.io/api/cucumber/jvm/javadoc/)

## Git Clone Project

##### You will need to download and install Git
- [Install Git For Windows Here](https://git-scm.com/download/win) 
    - Use default options or as preferred.
1. When on the projects github page click on `clone or download button`(_green button below contributor_) and copy the `HTTP` url
    - if you are having trouble finding it then copy this url and replace the `placeholderUsername` with your own
        - `https://github.com/placeholderUsername/java-gradle-selenium-cucumber-parallel-skeleton.git`
2. Open your CLI(command line interface) and `cd`(change directory) into the `directory/folder` where you want to download this project (It is recommended to create a directory for your git projects). 
    - Ex: `cd git-projects`
3. Use git clone to download the project with the following command with your username
    - `git clone https://github.com/placeholderUsername/java-selenium-cucumber-skeleton.git`

Setup
---

## `Download Java`
[Download and get Java installed from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
(you can use other jdk's. This is to just get you started)

## `Mac OSX`:
**1.** Open a terminal and proceed with the following:
`$ open ~/.bash_profile
`\
**2.** Set environment variables
```
export JAVA_HOME=$(/usr/libexec/java_home)
export PATH=${PATH}:$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$JAVA_HOME/bin
```
\
**3.** Save changes, reopen terminal and enter the following.
- **Homebrew**: 
`$ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`
    - After installation: `$ brew doctor` should state `Your system is ready to brew`
- **Git**:`$ brew install git`
    
## `Windows OS`:
**Set windows variables:**
1. open _powershell as admin_ > enter `rundll32 sysdm.cpl,EditEnvironmentVariables` to open windows variables
2. Create and set following for `SYSTEM VARIABLES`. You will click on the `NEW` button to create a variable.
```
Variable name -> JAVA_HOME
Variable path -> path\to\javaSDK (put path to your actual sdk which is usually in your ProgramFiles folder)
```
3. Select `Path` in `SYSTEM Variables` and click `Edit` then click `New` and enter the following for the variables created:
 ```
%JAVA_HOME%\bin
```
3. Close the admin powershell instance

## **Install** [IntelliJ](https://www.jetbrains.com/idea/download):       

When intellij is open do the following 
`Import Project` > `find where you git cloned the project to and select it` > `Gradle` > continue through the steps to import project. 

Install cucumber plugins
- `File` > `Preferences/Settings`_ > _`Plugins`_ > _`Marketplace`_:
- _`Cucumber for Java`_
- _`google-java-format`_
    - **How to use**: Enable the plugin in `Other Settings` 
        - `Ctrl + Alt + L` to format or `right click on file` > `Reformat Code`
    
Usage:
---
#### **Local**
- Local drivers are created automatically and you should not need to do anything other than supply the deviceName

#### **Remote**
- Start Selenium Session
    - Set `-DisRemote="true"` in CLI (command line interface)
    - Install Selenium: `$ selenium-standalone install`
    - Start Selenium: `$ selenium-standalone start`     

**Running tests**
----  
**Gradle Wrapper Command Line Test Runs** 
- Open your `Terminal/Powershell`(if you open the terminal from intelliJ you don't need to cd) and `cd`(_**change directory**_) to `project path` on your system
- Example: `C:\Users\yourUserHere\git-projects\projectName`
    - now that we are in the project directory we can use `gradlew tasks` to get more info about the project and how to run tests with it. Scroll up and locate the cucumber groups for project info!  
        - **NOTE:** use `.\gradlew` with powershell !!!
        
- Reports and screenshots are located here for local viewing!!! ```C:\Users\yourUserHere\git-projects\projectName\TestResults```
            
- Run the project with this command
    ```
    gradlew clean build giphyEnv neatGifTest cucumber
    ```            
        
**IntelliJ**
\
Create a run configuration. This will allow you to run Scenarios by right clicking them and selecting run in IntelliJ   
- Create new Cucumber Java run configuration: `Run` > `Edit Configurations` > `Templates` > `Cucumber Java`
    - Main class: `io.cucumber.core.cli.Main`
    - Glue: `core.setup core.test.steps`
    - Program Arguments _(copy and paste this into the program arguments after expanding)_
    ```
    -p
    pretty
    --add-plugin
    de.monochromata.cucumber.report.PrettyReports:TestResults/Reports/cucumber-html
    -p
    json:TestResults/Reports/cucumber-report.json
    ```
    - Feature or folder path: `/path/to/features`
        - Example `/Users/your_username/project_name/src/test/resources/features`

**Framework Workflow**
----  
- **Config:** (_`core/utilities/setup/Config.java`_)
    - This is where we _**create the desired capabilities**_ for our devices based off the current platform. 
        - `getDeviceCapabilities()` deserializes _`jsonData/devices.json`_ JSON data 
     
- **Hooks:** (_`core/utilities/setup/Hooks.java`_)
    - We use the _**createDriver**_ from _**CreateSharedDrivers**_ to set the _**drivers**_ and perform actions based on test conditions. 
        - `beforeScenario()` configures data, drivers, and variables for test run.
        - `afterScenario()` Setup will be set to false after all tests ran and The driver will be quit. On scenario failure a screenshot will be taken
        
- **PageObjectBase:** (_`core/base/PageObjectBase.java`_)
    - This houses general use methods. The constructor sets the driver variable so this class can be used as a super. 
    - `getField(elementField)` Is how we use string parameters in gherkin steps to use elements on pages/modules

