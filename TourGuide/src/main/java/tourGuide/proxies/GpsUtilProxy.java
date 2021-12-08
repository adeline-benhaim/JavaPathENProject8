package tourGuide.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "gps-util", url = "${gps.util.proxy.url}")
public interface GpsUtilProxy {

    /**
     * Get the actual user location
     * @param userId id of the user whose location is sought
     * @return a visited location composed of a userId, a location with longitude and latitude and a date
     */
    @GetMapping("/location")
    VisitedLocationBean getUserLocation(@RequestParam UUID userId);

    /**
     * Get a list of all attractions
     * @return a list of all attractions with for each a name, a city, a state, a latitude and a longitude
     */
    @GetMapping("/attractions")
    List<AttractionBean> getAttractions();
}
