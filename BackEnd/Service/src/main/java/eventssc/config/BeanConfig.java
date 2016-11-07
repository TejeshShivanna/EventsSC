package eventssc.config;

import eventssc.database.AmazonRDS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class BeanConfig {

    @Bean
    public AmazonRDS amazonRDS(@Value("${aws.host}") String awshost,
                               @Value("${aws.port}") int awsport,
                               @Value("${aws.database}") String awsdatabase,
                               @Value("${aws.username}") String awsusername,
                               @Value("${aws.password}") String awspassword){
        return new AmazonRDS(awshost, awsdatabase, awsusername, awspassword, awsport);
    }
}
