package eventssc.controller;

import eventssc.database.AmazonRDS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RestController
public class ConnectionController {

    final static Logger logger = LoggerFactory.getLogger(AmazonRDS.class);

    private AmazonRDS amazonRDS;

    @Autowired
    public ConnectionController(AmazonRDS amazonRDS) throws Exception{
        this.amazonRDS = amazonRDS;
    }

    @RequestMapping("/connection")
    public String connected(@RequestParam(value="name", defaultValue="Events@SC") String name){
        return name;
    }

    @RequestMapping("/usc")
    public JSONObject getUscDetails() throws Exception{
        Connection connection = null;
        JSONObject uscDetails = new JSONObject();
        ResultSet resultSet;

        try{
            String selectUSC = "SELECT * FROM USERDETAILS WHERE FIRSTNAME='USC'";

            connection = amazonRDS.getConnection();
            Statement statement = connection.createStatement();

            resultSet = statement.executeQuery(selectUSC);

            if(resultSet.next()) {
                uscDetails.put("FIRSTNAME", resultSet.getString("FIRSTNAME"));
                uscDetails.put("LASTNAME", resultSet.getString("LASTNAME"));
                uscDetails.put("CONTACTNUMBER", resultSet.getString("CONTACTNUMBER"));
            }
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
        return uscDetails;
    }

}
