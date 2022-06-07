
# apiwiz-core-platform-api

### Setup Code Style in IDE
#### Eclipse

Download the eclipse_check_style.xml file from code repo. Under Window/Preferences select Java/Code Style/Formatter. Import the settings file by selecting Import.

To auto format, **Window->Preferences->Java->Editor->SaveActions**

#### Intellij

Download the eclipse_check_style.xml file from code repo. Navigate to Preferences -> Editor -> Code Style. Click on Manage and import the downloaded Style Setting file. Select GoogleStyle as new coding style.
  

### SETUP CODEBASE IN LOCAL ENVIRONMENT

**REPOSITORY DOWNLOAD**

 - Download the repository file from the drive-
   
   https://drive.google.com/file/d/12A1lDVf9CF9GDTgskRm-YFOE8pcoXhy4/view?ts=628dd28a

 - Extract the file.

 - Copy the folder and paste in the .m2 folder in the Users (you can find the hidden folders by selecting shift+command+.)

 - Download the environment file from the drive-
https://drive.google.com/file/d/1daxPArY5o_1XDS-laJKS8KuoltH8DMgi/view?ts=628e5690

 - Extract the file.

**STUDIO 3T**

 - Download and Install Studio 3T.

 - Set up a localhost on your Mongo dB and open Studio 3T.

 - Click on the connect at the top left side and connect with your localhost.

 - Double click on the localhost and choose import and select JSON and click finish.

 - On the JSON import screen select add sources and select all the Json file inside the acme-team-dev folder in the dev folder you just downloaded.

 - Do the same for the apiwiz folder.

**CLONING AND INSTALLING REPOSITORIES**

 - Clone the repositories (1.apiwiz-core-platform-api and 2.apiwiz-non-prod-config) from the Itorix git repository and import the apiwiz-core-platform-api to you IDE in a new workspace.

 - In the terminal go to apiwiz-core-platform-api and enter `mvn clean install -DskipTests=true -Dmaven.javadoc.skip=true`

 - Open terminal and enter `pwd.`

 - Enter `mkdir appdata.`

 - Copy the path of the appdata folder.

 - Go to the location of the cloned repository and inside apiwiz-non-prod-config folder go to core-api folder and open config.properties

 - Change the `server.port` to 9001.

 - Add the appdata path before `/logs` in the `itorix.core.log.location` data (5th line).

 - Change the path for all the instance of `/opt` to the path of your appdata folder and save it.

**LOMBOK INSTALLATION**

 - Lombok setup is needed only for Eclipse and not for Intellij

 - Download lombok from-
https://projectlombok.org/download and install it.

 - You can see if lombok is installed on the Eclipse by  selectitng **Eclipse->About Eclipse.**

 - Now go the cloud-app folder on the IDE and open Application.java and right click on that and select run as and select run configurations.

 - In the VM arguments`-Dconfig.properties={configpath}/config.properties` and run.

 - The Application will be up and running


**POSTMAN COLLECTIONS**
APIwizPostmanCollection.json
PostmanEnvironment.json
