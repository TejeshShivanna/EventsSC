package eventssc.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eventssc.range.Range;
@RestController
public class ConnectionController {

    @RequestMapping("/connection")
    public String connected(@RequestParam(value="name", defaultValue="Events@SC") String name){
        return name;
    }


    @RequestMapping("/range")
    public String rangeQuery(@RequestParam(value="name", defaultValue="{\n" +
            "                  \"latitude\" : 34.0230895,\n" +
            "                  \"longitude\" : -118.2870363\n" +
            "               }") String latLong){ return Range.getEventsinRange(latLong);
    }
}
