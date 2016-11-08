package eventssc.controller;

import eventssc.dao.DaoException;
import eventssc.database.AmazonRDS;
import eventssc.event.EventBean;
import eventssc.range.Range;
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
    private Range range;
    private EventBean eventBean;

    @Autowired
    public ConnectionController(AmazonRDS amazonRDS, Range range, EventBean eventBean) throws Exception {
        this.amazonRDS = amazonRDS;
        this.range = range;
        this.eventBean = eventBean;
    }

    @RequestMapping("/connection")
    public String connected(@RequestParam(value = "name", defaultValue = "Events@SC") String name) {
        return name;
    }

    @RequestMapping("/usc")
    public JSONObject getUscDetails() throws Exception {
        Connection connection = null;
        JSONObject uscDetails = new JSONObject();
        ResultSet resultSet;

        try {
            String selectUSC = "SELECT * FROM USERDETAILS WHERE FIRSTNAME='USC'";

            connection = amazonRDS.getConnection();
            Statement statement = connection.createStatement();

            resultSet = statement.executeQuery(selectUSC);

            if (resultSet.next()) {
                uscDetails.put("FIRSTNAME", resultSet.getString("FIRSTNAME"));
                uscDetails.put("LASTNAME", resultSet.getString("LASTNAME"));
                uscDetails.put("CONTACTNUMBER", resultSet.getString("CONTACTNUMBER"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
            if (connection != null) connection.rollback();
        } finally {
            if (connection != null) connection.close();
        }
        return uscDetails;
    }

    @RequestMapping("/range")
    public String rangeQuery(@RequestParam(value = "name", defaultValue = "{\n" +
            "                  \"latitude\" : 34.0230895,\n" +
            "                  \"longitude\" : -118.2870363\n" +
            "               }") String latLong) throws DaoException {
        return range.getEventsinRange(latLong);
    }

    @RequestMapping("/all_events")
    public String getAllEvents() throws DaoException {
        return eventBean.getAllEvents();
    }

    @RequestMapping("/create")
    public boolean createEvent(@RequestParam(value = "jsonObj", defaultValue = "{}") String jsonObj) throws DaoException {
        String jsonObject = "{\"locationId\":228, \"creatorId\":5, \"name\":\"My Research\", \"description\":\" My Academic\",\"starttime\":\"Jan 1, 1970 11:00:00 AM\",\"endtime\":\"Jan 1, 1970 11:50:00 AM\",\"latitude\":34.0206012, \"longitude\":-118.2860922, \"address\":\"My Address\"}";
        return eventBean.createEvent(jsonObject);
    }

}