package org.iobis.reaper;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application extends AsyncConfigurerSupport {

    @Value("${mongodb.host}")
    private String host;

    @Value("${mongodb.db}")
    private String dbName;

    @Value("${mongodb.port}")
    private Integer port;

    @Value("${async.corepoolsize}")
    private Integer corePoolSize;

    @Value("${async.maxpoolsize}")
    private Integer maxPoolSize;

    @Value("${async.queuecapacity}")
    private Integer queueCapacity;

    @Value("${mongodb.collection.log}")
    private String logCollection;

    @Value("${mongodb.collection.errors}")
    private String errorsCollection;

    @Value("${mongodb.log.size}")
    private Long logSize;

    @Value("${mongodb.errors.size}")
    private Long errorsSize;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class);
    }

    public MongoClient mongoClient() {
        MongoClient mongoClient = new MongoClient(host, port);
        return mongoClient;
    }

    @Bean
    public DB db() {
        DB db = mongoClient().getDB(dbName);
        if (!db.collectionExists(logCollection)) {
            DBObject options = BasicDBObjectBuilder.start().add("capped", true).add("max", logSize).add("size", 1000000000000L).get();
            db.createCollection(logCollection, options);
        }
        if (!db.collectionExists(errorsCollection)) {
            DBObject options = BasicDBObjectBuilder.start().add("capped", true).add("max", errorsSize).add("size", 1000000000000L).get();
            db.createCollection(errorsCollection, options);
        }
        return db;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.initialize();
        return executor;
    }

}
