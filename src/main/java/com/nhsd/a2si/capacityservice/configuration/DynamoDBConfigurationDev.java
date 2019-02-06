package com.nhsd.a2si.capacityservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

@Profile({"capacity-service-local-redis", "capacity-service-local-stub"})
@Configuration
//@EnableDynamoDBRepositories
//  (basePackages = "com.baeldung.spring.data.dynamodb.repositories")
public class DynamoDBConfigurationDev {

	 
	    @Value("${amazon.aws.dynamo.endpoint}")
	    private String amazonDynamoDBEndpoint;
	 
	    @Value("${amazon.aws.dynamo.accesskey}")
	    private String amazonAWSAccessKey;
	 
	    @Value("${amazon.aws.dynamo.secretkey}")
	    private String amazonAWSSecretKey;
	 
	    @Bean
	    public AmazonDynamoDB amazonDynamoDB() {
	        AmazonDynamoDB amazonDynamoDB 
	          = new AmazonDynamoDBClient(amazonAWSCredentials());
	         
	        if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
	            amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
	        }

	         
	        return amazonDynamoDB;
	    }
	 
	    @Bean
	    public AWSCredentials amazonAWSCredentials() {
	        return new BasicAWSCredentials(
	          amazonAWSAccessKey, amazonAWSSecretKey);
	    }
	}

