# a2si-capacity-service
A2SI Capacity Service - Stores and allows access to Capacity Information. It is a Spring Boot application
exposing a Rest API allowing access to the capacity information. All API calls must pass a valid
username and password as HTTP headers.

## Getting started
First, download the code from GitHub. This can be done using the desktop git tool, an IDE which supports git or by downloading the code as a zip file which you can then extract.

Next, install the dev tools and dependencies....

##Installation of Development Tools and Dependencies
Install Git for Windows:
Install official git release: https://git-scm.com/download/win

Or install GitHub Desktop which also includes a GUI interface for git management: https://desktop.github.com/

###Install Java Development Kit 8:
http://www.oracle.com/technetwork/java/javase/downloads/

###Install Maven 3:
https://maven.apache.org/download.cgi

###Environment Variables
Ensure that the system environment variables for Java and Maven are set correctly, as described below...

M2_HOME should point to the install directory of your local Maven install folder, e.g.
M2_HOME C:\Maven\apache-maven-3.3.9

JAVA_HOME should point to the install directory of your local Java JDK install folder, e.g.

JAVA_HOME C:\Program Files\Java\jdk1.8.0_121

PATH should contain the bin directory of both M2_HOME and JAVA_HOME, e.g.

```
...;%JAVA_HOME%\bin;%M2_HOME%\bin;...
```

## Dependencies
Before building this module, the following modules must have been downloaded and built, using "mvn install"
to add them into your local Maven Repository.

1) a2si-capacity-information

## Building the Application
```
cd {projectRoot}
mvn clean package
```

The Maven projects builds two deployable artifacts, one is the "Uber-Jar" common to a lot of applications built 
using Spring Boot, it contains Tomcat as a container within the jar as well as all of Tomcat's dependencies 
and the application's dependencies. 

## Running the Application
The application is going to be deployed in AWS using Elastic Beanstalk, using Docker as a container. 
Elastic Beanstalk allows Spring Boot applications to be packaged along with a DockerFile in a single zip file. 
This zip file is all that is required to deploy into AWS Elastic Beanstalk. 
Environment variables may be required to define the "Active Spring Profile" value.
Environment variables are also used to define the API username and password - any clients must pass the correct
API username and API password in HTTP headers.
- capacity-service-api-username=**user**
- capacity-service-api-password=**password**



Therefore to run it locally, just use:

```
java -Dspring.profiles.active=local-cps-stub 
     -Dcapacity.service.api.username=**user** 
     -Dcapacity.service.api.password=**password** 
     -jar a2si-capacity-service-0.0.1-SNAPSHOT.jar
```

Note the the application configuration (application.yml) defines the port it will run under as 7020.

The Capacity Service is a REST API that requires two http headers in all requests for authentication. 
The headers are validated against the username and password defined as environment variables when the
application was started.

Therefore, all capacity service clients must pass http headers with values that match the username and password 
supplied to the Capacity Service application when it starts.

The Capacity Service CLIENT http headers are named:

- capacity-service-api-username
- capacity-service-api-password 

The values for these headers are defined at run time and the capacity service client module uses these 
values when calling the capacity service.
The values for the headers are defined using environment variables and should be named:

- CAPACITY_SERVICE_CLIENT_API_USERNAME &
- CAPACITY_SERVICE_CLIENT_API_USERNAME 

when used as environment variables in AWS Elastic Beanstalk

or
- capacity.service.client.api.username &
- capacity.service.client.api.password 

when used as environment variables running the application from a terminal.

The Service has several modes of operation, these are:
10. Uses a Java Hashmap, therefore a non-clustered, non-persistent, in memory only model. This is only for
use as a development mode and should never be used in test environments or production.
20. The Second uses a single Redis instance in that runs in a Docker container.
30. The Third uses a cluster of Redis instances deployed within AWS. 

Different configurations are used to set Redis properties for the single instance and the cluster.

Spring Profiles are used to define the configuration used.

This requires an environment variable (SPRING_PROFILES_ACTIVE) to be included when starting up the capacity service.

There are several configuration files that relate to the different profiles used. These are:


+ application.yml - 
The default configuration provides defaults for the port the server runs under and some base values, 
it is not sufficient to actually allow the service to run but should be used to define properties that don't change
between environments.

+ application-local-cps-stub.yml - 
This file is to be used when running the service locally (on a developer machine) and uses in memory Java collections
to store the capacity information. Therefore this profile should only be used when testing the API locally and never
for test or production environments.

+ application-local-cps-stub.yml - 
This file is to be used when running the service locally (on a developer machine) but uses a Redis single instance
that is running in a Docker container. Therefore this profile should only be used when testing the API locally and never
for test or production environments but it does give a developer the chance to work with Redis without deploying to AWS,
making development quicker and easier to debug. 

+ application aws-cps-stub.yml - 
This file is to be used when running the service on AWS and uses in memory Java collections
to store the capacity information. Therefore this profile should only be used when testing the API and never
for test or production environments. It can be useful when initially setting up deployments in AWS and you 
want a capacity service running but have yet to configure Redis Clusters.

+ application aws-cps-redis.yml - 
This file is to be used when running the service on AWS and a cluster of Redis servers. a Redis cluster uses the Redis
client to define what the nodes are, i.e. it is the client that does load balancing rather than having a server bases
solution. The nodes themselves will share objects and handle the sharding etc.

## Using the API
As with many Rest APIs, the HTTP headers "Content-Type" and "Accept" can be used to define the preferred 
format of the resource. JSON is the data format the API was been designed for so the HTTP Headers should be:
```
Content-Type    :   application/json
Accept          :   application/json
```
Also, each call expects http headers defining the api username and password to use. Again, HTTP headers are used 
and the header names are:
```
capacity-service-api-username
capacity-service-api-password
```
They are required for all calls.
The application configuration files have the usernames and password to use, and the values can be set on 
a per profile basis.

The API endpoint depends on the deployment location but after the host address and port (7020), a URL of ```/capacity``` 
is used for all requests.

The Git repository has several collections of calls for use using Postman (https://www.getpostman.com), each collection 
is targeted to one environment and profile, e.g. Local, Local Docker machine, AWS etc. 
The maintenance of these collections as the APIs change or environments change is highly recommended, using the API 
with Postman is one of the best ways to learn about and test the Service.


