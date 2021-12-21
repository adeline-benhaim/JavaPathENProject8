package tourGuide.service;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;

import java.util.List;

public interface RewardsService {

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
    List<UserReward> getUserRewards(User user);

    /**
     * Calculate the rewards assigned to a user
     *
     * @param user the user whose rewards calculation is requested
     */
    void calculateRewards(User user);

    /**
     * Get the number of points awarded to a user based on an attraction
     *
     * @param attraction the attraction for which the rewards points are calculated
     * @param user       the user for whom the rewards points are calculated
     * @return number of points awarded to the user
     */
    int getRewardPoints(AttractionBean attraction, User user);

    /**
     * Get a distance between two locations
     *
     * @param loc1 the first location compared
     * @param loc2 the second location compared
     * @return the distance between the two locations provided
     */
    double getDistance(LocationBean loc1, LocationBean loc2);
}
