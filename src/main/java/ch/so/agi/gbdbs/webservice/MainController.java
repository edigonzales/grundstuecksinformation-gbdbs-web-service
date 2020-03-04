
package ch.so.agi.gbdbs.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.slf4j.Logger;

@Controller
public class MainController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @GetMapping("/")
    public ResponseEntity<String> ping() {
        logger.info("ping");
        return new ResponseEntity<String>("gbdbs web service", HttpStatus.OK);
    }
    
}
