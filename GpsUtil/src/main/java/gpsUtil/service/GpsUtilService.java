package gpsUtil.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.List;
import java.util.UUID;

public interface GpsUtilService {

    /**
     * Get the actual user location
     * @param userId id of the user whose location is sought
     * @return a visited location composed of a userId, a location with longitude and latitude and a date
     */
    VisitedLocation getUserLocation(UUID userId);

    /**
     * Get a list of all attractions
     * @return a list of all attractions with for each a name, a city, a state, a latitude and a longitude
     */
    List<Attraction> getAttractions();
}
