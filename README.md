# a2si-capacity-service
Stores and serves Realtime Capacity Information for services. 

It is a Spring Boot application exposing a Rest API allowing access to the capacity information. 

All API calls must pass a valid username and password as HTTP headers.

## Getting started
1. Set up your [Development Environment](docs/dev_setup.md)

2. Clone this repo to your local machine.

3. Follow the instructions below to configure and run the application.

## Application Dependencies
Before building this module, the following modules must have been downloaded and built, using `mvn clean install`
to add them into your local Maven Repository.

* [a2si-capacity-information](https://github.com/nhsd-a2si/a2si-capacity-information)

Detailed instructions are in the README of the respective repositories.

## Building the Application
```
cd {projectRoot}
mvn clean package
```

The Maven project builds two deployable artefacts: 

+ the "Uber-Jar" common to a lot of applications built 
using Spring Boot, it contains Tomcat as a container within the jar as well as all of Tomcat's dependencies 
and the application's dependencies

+ a zip file containing the "Uber-Jar" and a Dockerfile 

### Configuring the Application

#### Logging
By default, Logback will write logs to a relative file path of `logs/a2si/a2si-capacity-service.log`

You can override the **`LOG_PATH`** variable by setting it as an environment variable e.g.
```bash
$ export LOG_PATH="/var/logs/a2si"
```

`/a2si-capacity-service.log` will be appended to the **`LOG_PATH`** value you specify.

#### Database

The Service has several modes of operation, these are:

1. Using a Java Hashmap - therefore a non-clustered, non-persistent, in memory only model. **Not for use in production**
2. Using a single Redis instance that runs in a Docker container.
3. Using a cluster of Redis instances deployed within AWS.

Different configurations are used to set Redis properties for the single instance and the cluster.

#### Spring Profiles
Spring Profiles are used to define the configuration used.

This requires an environment variable **`SPRING_PROFILES_ACTIVE`** to be included when building or starting up the capacity service.

The possible values for the **`SPRING_PROFILES_ACTIVE`** variable are:

+ `capacity-service-local-stub`
+ `capacity-service-local-redis`
+ `capacity-service-aws-stub`
+ `capacity-service-aws-redis`

There are several configuration files which relate to the different profiles used. These are:

+ **application.yml**  
The default configuration provides defaults for the port the server runs under and some base values which are shared amongst all configuration profiles.

+ **application-local-cps-stub.yml**  
*Profile name: `capacity-service-local-stub`*  
Used when running the service locally (on a developer machine) and uses the Java Hashmap to capacity data. 
*This profile should only be used when testing the API locally and never for deployed environments.*

+ **application-local-cps-redis.yml**   
*Profile name: `capacity-service-local-redis`*    
Used when running the service locally (on a developer machine) but uses a Redis single instance
that is running in a Docker container. Therefore this profile should only be used when testing the API locally and never
for test or production environments but it does give a developer the chance to work with Redis without deploying to AWS,
making development quicker and easier to debug. 

+ **application-aws-cps-stub.yml**   
*Profile name: `capacity-service-aws-stub`*  
This file is to be used when running the service on AWS and uses in memory Java collections
to store the capacity information. Therefore this profile should only be used when testing the API and never
for test or production environments. It can be useful when initially setting up deployments in AWS and you 
want a capacity service running but have yet to configure Redis Clusters.

+ **application-aws-cps-redis.yml**  
*Profile name: `capacity-service-aws-redis`*    
This file is to be used when running the service on AWS and a cluster of Redis servers. a Redis cluster uses the Redis
client to define what the nodes are, i.e. it is the client that does load balancing rather than having a server bases
solution. The nodes themselves will share objects and handle the sharding etc.

#### Listening Port
The application configuration file (application.yml) defines the port the service will run under as 7020.

```yaml
server:
  port: 7020
```

#### API Authentication

The Capacity Service is a REST API that requires two http headers in all requests for authentication. 
The headers are validated against the username and password defined as environment variables when the
application was started.

All requests to the capacity service must contain these HTTP headers with values that match the username and password 
supplied to the Capacity Service application when it starts.

The values for these headers are defined at run time and the capacity service client module uses these 
values when calling the capacity service.

The application configuration files have the usernames and password to use, and the values can be set on 
a per profile basis.

```yaml
capacity:
  service:
    api:
      username: dummyValue
      password: dummyValue
```

The values for the headers can also be defined using environment variables which should be named:

+ `CAPACITY_SERVICE_CLIENT_API_USERNAME`
+ `CAPACITY_SERVICE_CLIENT_API_PASSWORD` 

It is also possible to pass the settings as command line parameters when running the application:

`-Dcapacity.service.api.username=dummyValue`  
`-Dcapacity.service.api.password=dummyValue`


## Running the Application
The application is currently designed to be deployed in AWS using Elastic Beanstalk, using Docker as a container. 

Elastic Beanstalk allows Spring Boot applications to be packaged along with a DockerFile in a single zip file. This zip file is all that is required to deploy into AWS Elastic Beanstalk.

### Running Locally
To run the service locally you can specify the configuration properties on the command line instead of using environment variables:

```
java -Dspring.profiles.active=capacity-service-local-stub \
     -Dcapacity.service.api.username=dummyValue \  
     -Dcapacity.service.api.password=dummyValue \
     -jar a2si-capacity-service-0.0.1-SNAPSHOT.jar
```

where the value of the `-jar` parameter is the filename of your built .jar file.

## Making API Requests
As with many Rest APIs, the HTTP headers `Content-Type` and `Accept` can be used to define the preferred 
format of the resource. 

JSON is the data format the API was been designed for so the HTTP Headers should be:
```
Content-Type    :   application/json
Accept          :   application/json
```
Also, each call expects http headers defining the api username and password to use:
```
capacity-service-api-username
capacity-service-api-password
```

These headers are required for all requests to the API.
