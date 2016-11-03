import database.AmazonRDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.Constants;

public class Crawler {

    final static Logger logger = LoggerFactory.getLogger(Crawler.class);

    public static void main(String[] args) {
        try {
            // to crawl events and write eventsinfo to file
            //Crawl crawlUSC = new Crawl();
            // crawlUSC.crawlAndWriteToFile("Events");

            // write events into from file to AWS DATABASE
            AmazonRDS amazonRDS = new AmazonRDS(Constants.host, Constants.dataBaseName, Constants.username, Constants.password, Constants.port);
            amazonRDS.writeToEventTable();
        }
        catch (Exception ex){
            logger.error(ex.getStackTrace().toString());
        }
    }
}