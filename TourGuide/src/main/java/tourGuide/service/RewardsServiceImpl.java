package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsServiceImpl implements RewardsService {
    private final Logger logger = LoggerFactory.getLogger(RewardsServiceImpl.class);
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;

    private final GpsUtilProxy gpsUtil;
    private final RewardCentralProxy rewardsCentral;

    public RewardsServiceImpl(GpsUtilProxy gpsUtil, RewardCentralProxy rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    /**
     * Get a list of rewards per user.
     * Each reward contain :
     * - a visited location
     * - an attraction
     * - a reward point
     *
     * @param user the user whose rewards are sought
     * @return a list of user reward
     */
    @Override
    public List<UserReward> getUserRewards(User user) {
        logger.info("Get rewards for user name : {}", user.getUserName());
        return user.getUserRewards();
    }

    /**
     * Calculate the rewards assigned to a user
     *
     * @param user the user whose rewards calculation is requested
     */
    @Override
    public void calculateRewards(User user) {
        logger.info("Calculate rewards for user name : {}", user.getUserName());
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        // Fix ConcurrentModificationException
        CopyOnWriteArrayList<VisitedLocationBean> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        CompletableFuture.supplyAsync(gpsUtil::getAttractions).thenAccept(attractions -> {
            for (VisitedLocationBean visitedLocation : userLocations) {
                for (AttractionBean attraction : attractions) {
                    if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                        if (nearAttraction(visitedLocation, attraction)) {
                            CompletableFuture.supplyAsync(() -> getRewardPoints(attraction, user), executorService)
                                    .thenAccept(rewardPoints -> {
                                        UserReward userReward = new UserReward(visitedLocation, attraction, rewardPoints);
                                        user.addUserReward(userReward);
                                    });
                        }
                    }
                }
            }
        });
    }

    /**
     * Check if a visited location is near an attraction
     *
     * @param visitedLocation the user location used to check the distance
     * @param attraction      the attraction location used to check the distance
     * @return true if the distance between the visited location and the attraction location is considered nearby
     */
    public boolean nearAttraction(VisitedLocationBean visitedLocation, AttractionBean attraction) {
        logger.info("Check if is a near attraction");
        return !(getDistance(attraction, visitedLocation.locationBean) > defaultProximityBuffer);
    }

    /**
     * Get the number of points awarded to a user based on an attraction
     *
     * @param attraction the attraction for which the rewards points are calculated
     * @param user       the user for whom the rewards points are calculated
     * @return number of points awarded to the user
     */
    @Override
    public int getRewardPoints(AttractionBean attraction, User user) {
        logger.info("Get rewards points for user name : {} and attraction name {}", user.getUserName(), attraction.getAttractionName());
        return rewardsCentral.getRewards(attraction.attractionId, user.getUserId());
    }

    /**
     * Get a distance between two locations
     *
     * @param loc1 the first location compared
     * @param loc2 the second location compared
     * @return the distance between the two locations provided
     */
    @Override
    public double getDistance(LocationBean loc1, LocationBean loc2) {
        logger.info("Get distance between 2 locations");
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
