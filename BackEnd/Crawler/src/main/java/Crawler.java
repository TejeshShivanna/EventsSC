import crawl.Crawl;
import database.AmazonRDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.Constants;

public class Crawler {

    final static Logger logger = LoggerFactory.getLogger(Crawler.class);

    public static void main(String[] args) {
        try {
            Crawl crawlUSC = new Crawl();
            AmazonRDS amazonRDS = new AmazonRDS(Constants.host, Constants.dataBaseName, Constants.username, Constants.password, Constants.port);

            // to crawl events and write eventsinfo from file to EVENT table
            crawlUSC.crawlAndWriteToFile("Events");
            //amazonRDS.writeToEventTable();

            // to crawl usc locations and write locationinfo from file to LOCATION table
            crawlUSC.crawlAndWriteToFile("Building");
            //amazonRDS.writeToLocationTable();
        }
        catch (Exception ex){
            logger.error(ex.getStackTrace().toString());
        }
    }
}
