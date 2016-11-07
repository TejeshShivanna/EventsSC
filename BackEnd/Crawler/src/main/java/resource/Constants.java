package resource;

public class Constants {
    public static final String GoogleGeoCodeAPI = "AIzaSyB3JIuiPEDcUF_2IEuUFzm-fs3wmwPsyJY";

    public static final String host = "aag5mhra74ej9f.c5ylyjbogtox.us-west-2.rds.amazonaws.com";
    public static final int port = 5432;
    public static final String dataBaseName = "postgres";
    public static final String username = "postgres";
    public static final String password = "eventssc";

    public static final String crawlUrl = "https://careers.usc.edu/eventcalendar/";
    public static final String buildingUrl = "http://fmsmaps4.usc.edu/usc/php/bl_list_no.php";
    public static final String home = System.getProperty("user.home");
    public static final String eventsFile = home + "/data/output/eventsInfo.json";
    public static final String bldgFile = home + "/data/output/buildingInfo.json";
}
