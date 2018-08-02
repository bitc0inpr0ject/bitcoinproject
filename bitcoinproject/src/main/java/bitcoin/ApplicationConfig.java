package bitcoin;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

@Configuration
@ComponentScan(basePackages = "bitcoin")
public class ApplicationConfig {
    public @Bean
    MongoDbFactory mongoDbFactory() throws Exception {
        UserCredentials credentials = new UserCredentials("bitcointeam","bitcoin@");
        return new SimpleMongoDbFactory(new Mongo("localhost", 27017), "testnewdb",credentials);
    }
    public @Bean
    MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        return mongoTemplate;
    }
}
