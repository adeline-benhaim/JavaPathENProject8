package tourGuide.service;

import tourGuide.beans.VisitedLocationBean;
import tourGuide.model.Dto.NearbyAttractionListByUserDto;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface TourGuideService {

    /**
     * Get a visitedLocation by user
     *
     * @param user the user whose location is sought
     * @return actual user location if its list of visitedLocation is empty otherwise its last visitedLocation
     * @throws ExecutionException can be thrown when attempting to retrieve the result of trackUserLocation that aborted by throwing an exception.
     * @throws InterruptedException can be thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    VisitedLocationBean getUserLocation(User user) throws ExecutionException, InterruptedException;

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
     * Get a list of provider with price offer by user.
     * Each provider contains :
     * - a name
     * - a price
     * - an id
     *
     * @param user the user whose providers are sought
     * @return a list of providers with price offer
     */
    List<Provider> getTripDeals(User user);

    /**
     * Get a user by userName
     *
     * @param userName of user sought
     * @return the user found
     */
    User getUser(String userName);


    /**
     * Get the closest five tourist attractions to the user sorted in ascending order with user location information (longitude and latitude).
     * Each tourist attraction contains :
     * - a name
     * - a location (longitude and latitude)
     * - a distance in miles between the user's location
     * - the reward points for visiting this attraction
     *
     * @param visitedLocationBean a user location
     * @return the closest five tourist attractions to the user sorted in ascending order with all user and attractions information
     */
    NearbyAttractionListByUserDto nearbyAttractionListByUserDto(VisitedLocationBean visitedLocationBean);
}
