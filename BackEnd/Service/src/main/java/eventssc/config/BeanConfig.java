package eventssc.config;

import eventssc.dao.EventDao;
import eventssc.database.AmazonRDS;
import eventssc.event.EventBean;
import eventssc.event.EventManager;
import eventssc.range.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class BeanConfig {

    @org.springframework.context.annotation.Bean
    public AmazonRDS amazonRDS(@Value("${aws.host}") String awshost,
                               @Value("${aws.port}") int awsport,
                               @Value("${aws.database}") String awsdatabase,
                               @Value("${aws.username}") String awsusername,
                               @Value("${aws.password}") String awspassword){
        return new AmazonRDS(awshost, awsdatabase, awsusername, awspassword, awsport);
    }

    @Bean
    public EventDao eventDao(AmazonRDS amazonRDS){
        return new EventDao(amazonRDS);
    }

    @Bean
    public EventManager eventManager(EventDao eventDao){
        return new EventManager(eventDao);
    }

    @Bean
    public Range range(EventManager eventManager){
        return new Range(eventManager);
    }

    @Bean
    EventBean eventBean(EventManager eventManager){
        return new EventBean(eventManager);
    }
}