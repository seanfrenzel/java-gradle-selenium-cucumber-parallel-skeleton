Java-Cucumber
======
Test automation skeleton for java-cucumber

Resources
---
- [Cucumber-JVM](https://cucumber.io/docs/reference/jvm)
- [Cucumber-JVM API](http://cucumber.github.io/api/cucumber/jvm/javadoc/)

Setup
---
**Install** [IntelliJ](https://www.jetbrains.com/idea/download):
\
Install cucumber plugin
_`Preferences`_ > _`Plugins`_ > _`Cucumber for Java`_

Environment variables
---
**We will use npm to install needed packages:**
[install _current_ Node.js](https://nodejs.org/en/)

#### Mac OSX:
**1.** Open a terminal and proceed with the following:
`$ open ~/.bash_profile
`\
**2.** Set environment variables
```
export ANDROID_HOME=/path/to/your/android/sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export JAVA_HOME=$(/usr/libexec/java_home)
export PATH=${PATH}:$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$JAVA_HOME/bin
```
\
**3.** Save changes, reopen terminal and enter the following.
- **Homebrew**: 
`$ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`
    - After installation: `$ brew doctor` should state `Your system is ready to brew`
- **Node**:`$ brew install node`
- **Git**:`$ brew install git`
- **libimobiledevice - iOS**:`$ brew install libimobiledevice`
- **carthage - iOS**:`$ brew install carthage`
- **ios-deploy - iOS**:`$ npm install -g ios-deploy`
    - ios-deploy should be global.
    
#### Windows OS:
NOTE: Unable to run iOS on windows\

**Input command lines into powershell**\
_restart pc if npm is not found_
* `npm install npm@latest -g`
* `npm install selenium-standalone@latest -g`
* `selenium-standalone install`

**Set windows variables:**
1. open _powershell as admin_ > enter `rundll32 sysdm.cpl,EditEnvironmentVariables` to open windows variables
2. set the following for `SYSTEM VARIABLES`
```
Variable name -> JAVA_HOME
Variable path -> path\to\javaSDK
```
* set the following for `USER VARIABLES`
```
Variable name -> ANDROID_HOME
Variable path -> path\to\androidSDK
```
3. Select `Path` in `SYSTEM Variables` and click `Edit` then click `New` and enter the following for the variables created:
 ```
%JAVA_HOME%\bin
```
4. Select `Path` in `USER Variables` and click `Edit` then click `New` and enter the following for the variables created:
 ```
%ANDROID_HOME%\emulator
%ANDROID_HOME%\tools
%ANDROID_HOME%\platform-tools
```
    
Usage:
---
#### **Local**
- Start Selenium Session
    - Install Selenium: `$ selenium-standalone install`
    - Start Selenium: `$ selenium-standalone start`
    
Running tests
----  
**Gradle Wrapper Command Line Test Runs** 
- Open your `Terminal` and `cd`(_**change directory**_) to `project path` on your system
- Example: `C:\Users\yourUserHere\git-projects\project`
    - now that we are in the project directory we can use `gradlew` commands! 
        - Tryout this command to run the example test! 
        ```
        gradlew -DdeviceName"chrome" -Dcucumber.options="--tags @NeatGifTest" build clean cucumber
        ```  
        - You can also enter user info from the CLI if you don't want to use the JSON file as shown below 
        ```
        gradlew -DdeviceName"chrome" -Dusername="bob" -Dpassword="HOTsauce100" -Dcucumber.options="--tags @NeatGifTest" build clean cucumber
        ```   
        - **NOTE:** use `.\gradlew` with powershell !!!
        
- Reports and screenshots are located here for local viewing!!! ```C:\Users\yourUserHere\git-projects\hydrolator\TestResults```
            - `Reports\cucumber-html` will contain an `index.html` that you can open in a browser        

**IntelliJ**
\
Create a run configuration. This will allow you to run Scenarios by right clicking them and selecting run in IntelliJ   
- Create new Cucumber Java run configuration: `Run` > `Edit Configurations`
    - Main class: `cucumber.api.cli.Main`
    - Glue: `core.utilities.setup core.steps`
    - Feature or folder path: `/path/to/features` 
        - Example `/Users/your_username/project_name/src/test/resources/features`

We can also use program arguments to get screenshots or to only run specific tests  
- Example
    ```
     --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm4SMFormatter --plugin html:target/cucumber-report/cucumber.html --monochrome --tags @TagsYouWantToRun
    ```

**Framework Workflow**
----  
- **Config:**
    - This is where we _**create the desired capabilities**_ for our devices based of the current platform. 
        - `getDeviceCapabilities()` deserializes _`jsonData/devices.json`_ JSON data
        
        
- **Factory:**
    - We use the set _** capabilities**_ to _**create the driver**_ here
        - `createDriver()` will create the driver for the current platform     
     
- **Hooks:**
    - We use the _**created driver**_ from _**DriverFactory**_ to set the _**RemoteWebdriver**_ and perform actions based on test conditions. 
        - `beforeAll()` sets and created data, drivers, and variables for test run.
        - `afterAll()` Setup will be set to false after all tests ran. The driver will be quit. On scenario failure a screenshot will be taken.
        
    
- **PageObjectBase:**
    - This houses general use methods. The constructor sets the driver variable so this class can be used as a super. 
    - `getField(elementField)` Is how we use string parameters in gherkin steps to use elements on pages/modules
