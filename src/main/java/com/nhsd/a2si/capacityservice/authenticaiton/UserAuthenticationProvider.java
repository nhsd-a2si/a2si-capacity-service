package com.nhsd.a2si.capacityservice.authenticaiton;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Component
@Profile({"capacity-service-aws-redis", "capacity-service-aws-stub", "capacity-service-local-redis"})
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${amazon.aws.dynamo.table}")
    private  String dynamoAuthenticationTableName;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String username    = authentication.getName();
        final String credentials = authentication.getCredentials().toString();
        Item item = getItemByUsername(username);
        if(Objects.nonNull(item)) {
            if(passwordEncoder.matches(authentication.getCredentials().toString(), item.getString("SALTED_PASSWORD"))){
                return new UsernamePasswordAuthenticationToken(username, credentials, Collections.emptySet());
            }
        }
        throw new BadCredentialsException("Authentication failed for user = " + username);
    }

    private Item getItemByUsername(String name) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
        Table table = dynamoDB.getTable(dynamoAuthenticationTableName);
        GetItemSpec spec = new GetItemSpec().withPrimaryKey("USERNAME", name);
        return table.getItem(spec);
    }


    @Override
    public boolean supports(Class<?> token) {
        return token.equals(UsernamePasswordAuthenticationToken.class);
    }
}
