package com.nhsd.a2si.capacityservice.authenticaiton;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        System.out.println("hello there");
        String name = authentication.getName();
        String password = passwordEncoder.encode(authentication.getCredentials().toString());


        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

      Table table = dynamoDB.getTable("Authentication");

      GetItemSpec spec = new GetItemSpec().withPrimaryKey("name", name);

       Item item = table.getItem(spec);
        String password1 = item.getString("password");


        System.out.println("name:" + name);
        System.out.println("password:" + password);
        System.out.println("password1:" + password1);
        System.out.println(passwordEncoder.matches(authentication.getCredentials().toString(), password));
        System.out.println(passwordEncoder.matches(authentication.getCredentials().toString(), "$2a$10$KPsaeTtQZYYIiHnSoiThx.DZ7TtlkaKV.1J88bdrtKdWEty7x404e"));
        return null;
    }

//    public Map<String, AttributeValue> getUser(String email) {
//        Map<String,String> expressionAttributesNames = new HashMap<>();
//        expressionAttributesNames.put("#email","email");
//        Map<String,AttributeValue> expressionAttributeValues = new HashMap<>();
//        expressionAttributeValues.put(":emailValue",new AttributeValue().withS(email));
//        QueryRequest queryRequest = new QueryRequest()
//                .withTableName(TABLE_NAME)
//                .withKeyConditionExpression("#email = :emailValue")
//                .withExpressionAttributeNames(expressionAttributesNames)
//                .withExpressionAttributeValues(expressionAttributeValues);
//        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
//        List<Map<String,AttributeValue>> attributeValues = queryResult.getItems();
//        if(attributeValues.size()>0) {
//            return attributeValues.get(0);
//        } else {
//            return null;
//        }
//    }

    @Override
    public boolean supports(Class<?> token) {
        return token.equals(UsernamePasswordAuthenticationToken.class);
    }
}
