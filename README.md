# apiwiz-core-platform-api




### Setup Code Style in IDE
#### Eclipse
Download the eclipse_check_style.xml file from code repo. Under Window/Preferences select Java/Code Style/Formatter. Import the settings file by selecting Import.

To auto format, **Window->Preferences->Java->Editor->SaveActions**
#### Intellij
Download the eclipse_check_style.xml file from code repo. Navigate to Preferences -> Editor -> Code Style. Click on Manage and import the downloaded Style Setting file. Select GoogleStyle as new coding style.

https://itorix.atlassian.net/browse/EN-542


### SETUP CODEBASE IN LOCAL ENVIRONMENT 

 

1.Download the repository file from the drive- https://drive.google.com/file/d/12A1lDVf9CF9GDTgskRm-YFOE8pcoXhy4/view?ts=628dd28a 

 

2.Extract the file. 

 

3.Copy the folder and paste in the .m2 folder in the Users (you can find the hidden folders by selecting shift+command+.) 

 

4.Download and Install Studio 3T. 

 

5.Download the environment file from the drive- https://drive.google.com/file/d/1daxPArY5o_1XDS-laJKS8KuoltH8DMgi/view?ts=628e5690 

 

6.Extract the file. 

 

7.Set up a localhost on your Mongo dB and open Studio 3T. 

 

8.Click on the connect at the top left side and connect with your localhost. 

 

9.Double click on the localhost and choose import and select JSON and click finish. 

 

10.On the JSON import screen select add sources and select all the Json file inside the acme-team-dev folder in the dev folder you just downloaded.  

 

11.Do the same for the apiwiz folder. 

 

12.Clone the repositories (1.apiwiz-core-platform-api and 2. apiwiz-non-prod-config) from the Itorix git repository and import the apiwiz-core-platform-api to you IDE in a new workspace. 

 

13.In the terminal go to apiwiz-core-platform-api and enter mvn clean install -DskipTests=true -Dmaven.javadoc.skip=true 

 

14.Open terminal and enter pwd. 

 

15.Enter mkdir appdata. 

 

16.Copy the path of the appdata folder. 

 

17.Go to the location of the cloned repository and inside apiwiz-non-prod-config folder go to core-api folder and open config.properties 

 

18.Change the server.port to 9001. 

 

19.Add the appdata path before /logs in the itorix.core.log.location data (5th line). 

 

20.Change the path for all the instance of /opt to the path of your appdata folder and save it. 

 

21.Download lombok from- 

https://search.maven.org/search?q=g:org.projectlombok%20AND%20a:lombok and install it. 

 

22.Now go the cloud-app folder on the IDE and open Application.java and right click on that and select run as and select run configurations. 

 

23.Inside the VM arguments paste -Dconfig.properties= {pathToConfig}/config.properties and run. 

 

24.The Application will be up and running. 