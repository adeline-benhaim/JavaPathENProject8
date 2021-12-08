package tourGuide.service;

import org.springframework.stereotype.Service;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtilProxy gpsUtil;
    private final RewardCentralProxy rewardsCentral;

    public RewardsService(GpsUtilProxy gpsUtil, RewardCentralProxy rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    public void calculateRewards(User user) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<VisitedLocationBean> userLocations = user.getVisitedLocations();
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

    public boolean isWithinAttractionProximity(AttractionBean attraction, LocationBean location) {
        return !(getDistance(attraction, location) > attractionProximityRange);
    }

    private boolean nearAttraction(VisitedLocationBean visitedLocation, AttractionBean attraction) {
        return !(getDistance(attraction, visitedLocation.locationBean) > proximityBuffer);
    }

    public int getRewardPoints(AttractionBean attraction, User user) {
        return rewardsCentral.getRewards(attraction.attractionId, user.getUserId());
    }

    public double getDistance(LocationBean loc1, LocationBean loc2) {
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
