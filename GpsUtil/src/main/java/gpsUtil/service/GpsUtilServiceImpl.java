package gpsUtil.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GpsUtilServiceImpl implements GpsUtilService {

    private final GpsUtil gpsUtil;

    public GpsUtilServiceImpl(GpsUtil gpsUtil) {
        this.gpsUtil = gpsUtil;
    }

    /**
     * Get the actual user location
     * @param userId id of the user whose location is sought
     * @return a visited location composed of a userId, a location with longitude and latitude and a date
     */
    @Override
    public VisitedLocation getUserLocation(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }

    /**
     * Get a list of all attractions
     * @return a list of all attractions with for each a name, a city, a state, a latitude and a longitude
     */
    @Override
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }
}
