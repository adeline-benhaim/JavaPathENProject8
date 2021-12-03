package gpsUtil.controller;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import gpsUtil.service.GpsUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GpsUtilController {

    @Autowired
    private GpsUtilService gpsUtilService;

    /**
     * Get the actual user location
     * @param userId id of the user whose location is sought
     * @return a visited location composed of a userId, a location with longitude and latitude and a date
     */
    @RequestMapping("/location")
    public VisitedLocation getUserLocation(@RequestParam UUID userId) {
        return gpsUtilService.getUserLocation(userId);
    }

    /**
     * Get a list of all attractions
     * @return a list of all attractions with for each a name, a city, a state, a latitude and a longitude
     */
    @RequestMapping("/attractions")
    public List<Attraction> getAttractions() {
        return gpsUtilService.getAttractions();
    }
}
