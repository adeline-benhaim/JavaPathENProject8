package gpsUtil.controller;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import gpsUtil.service.GpsUtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GpsUtilController {
    private final Logger logger = LoggerFactory.getLogger(GpsUtilController.class);

    private GpsUtilService gpsUtilService;

    public GpsUtilController(GpsUtilService gpsUtilService) {
        this.gpsUtilService = gpsUtilService;
    }

    /**
     * Get the actual user location
     * @param userId id of the user whose location is sought
     * @return a visited location composed of a userId, a location with longitude and latitude and a date
     */
    @GetMapping("/location")
    public VisitedLocation getUserLocation(@RequestParam UUID userId) {
        logger.info("GpsUtil : Get user location " + userId);
        return gpsUtilService.getUserLocation(userId);
    }

    /**
     * Get a list of all attractions
     * @return a list of all attractions with for each a name, a city, a state, a latitude and a longitude
     */
    @GetMapping("/attractions")
    public List<Attraction> getAttractions() {
        logger.info("GpsUtil : Get all attractions");
        return gpsUtilService.getAttractions();
    }
}
