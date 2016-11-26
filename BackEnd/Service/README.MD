To create a jar and test the application locally, run the following commands from the project home directory:

1. Change the packaging in "pom.xml" file as "<packaging>jar</packaging>"
2. Comment "spring-boot-starter-tomcat" dependency in pom.xml.
3. To build and create package: mvn clean install
    The above command will create application as jar package in "/target" folder.
4. To run the application: java -jar target/EventsSC-1.0.jar

To deploy this application in AWS

1. Change the packaging in "pom.xml" file as "<packaging>war</packaging>"
2. To build and create package: mvn clean install
    The above command will create application as war package in "/target" folder.
3. Deploy the war created in the previous step in AWS.
4. To test if its working, from the browser goto, "http://eventssc.us-west-2.elasticbeanstalk.com/connection"
    ,this call should return "Events@SC" as response.
