package BitcoinService;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.Arrays;

public class MongoDbService {
    private static MongoTemplate mongoDb = null;

    public static void createMongoTemplateInstance(String host, int port, String username, String password, String dbname) {
        if (mongoDb == null) {
            try {
                MongoCredential credential = MongoCredential.createCredential(username, dbname, password.toCharArray());
                ServerAddress serverAddress = new ServerAddress(host, port);

                MongoClient mongoClient = new MongoClient(serverAddress,Arrays.asList(credential));

                MongoDbFactory factory = new SimpleMongoDbFactory(mongoClient, dbname);
                mongoDb = new MongoTemplate(factory);
            } catch (Exception e) {
                System.out.println("cannot create MongoTemplate instance");
                e.printStackTrace();
                mongoDb = null;
            }
        }
    }
    public static MongoTemplate getMongoTemplateInstance() {
        return mongoDb;
    }
}
