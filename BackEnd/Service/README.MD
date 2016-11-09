To create a war file to deploy to server, follow the below steps:
1. Goto project home directory.
2. To build and create package: mvn clean package
    The above command will create application as war package in "/target" folder.
3. Deploy the war file in AWS.

To create a jar, goto project home directory:

1. Remove "<scope>provided</scope>" line from "spring-boot-starter-tomcat" dependency.
2. Change "<packaging>war</packaging>" to "<packaging>jar</packaging>".
3. To build and create package: mvn clean package
    The above command will create application as jar package in "/target" folder.

4. To run the application: java -jar target/EventsSC-1.0.jar
