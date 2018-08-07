package com.nhsd.a2si.capacityservice.authenticaiton;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBKeyed;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.KeyType;

@DynamoDBTable(tableName = "Authentication")
public class UserAuthenticationModel {

    private String user;
    private String password;

    @DynamoDBHashKey
    public String getUser() {
        return user;
    }

    @DynamoDBAttribute
    public String getPassword() {
        return password;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
