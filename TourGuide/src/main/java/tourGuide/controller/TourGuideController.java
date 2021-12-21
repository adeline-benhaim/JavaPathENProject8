package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.model.Dto.NearbyAttractionListByUserDto;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.model.user.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {
    private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);

    @Autowired
    TourGuideService tourGuideService;
    @Autowired
    TripPricerService tripPricerService;
    @Autowired
    private RewardsService rewardsService;

    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Get a location by username
     *
     * @param userName the username whose location is sought
     * @return a user location (latitude and longitude)
     * @throws ExecutionException   can be thrown when attempting to retrieve the result of getUserLocation that aborted by throwing an exception.
     * @throws InterruptedException can be thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    @GetMapping("/getLocation")
    public ResponseEntity<LocationBean> getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
        logger.info("REST : Get user location");
        try {
            VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(tourGuideService.getUser(userName));
            return ResponseEntity.ok(visitedLocation.locationBean);
        } catch (UserNotFoundException e) {
            logger.error("REST : " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get the closest five tourist attractions to the user sorted in ascending order with user location information (longitude and latitude).
     * Each tourist attraction contains :
     * - a name
     * - a location (longitude and latitude)
     * - a distance in miles between the user's location
     * - the reward points for visiting this attraction
     *
     * @param userName the username whose five nearest tourist attractions are searched
     * @return the closest five tourist attractions to the user sorted in ascending order with all user and attractions information
     * @throws ExecutionException   can be thrown when attempting to retrieve the result of getUserLocation that aborted by throwing an exception.
     * @throws InterruptedException can be thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    @GetMapping("/getNearbyAttractions")
    public ResponseEntity<NearbyAttractionListByUserDto> getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        logger.info("REST : Get nearby attractions");
        try {
            VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(tourGuideService.getUser(userName));
            return ResponseEntity.ok(tourGuideService.nearbyAttractionListByUserDto(visitedLocation));
        } catch (UserNotFoundException e) {
            logger.error("REST : " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get a list of rewards per user.
     * Each reward contain :
     * - a visited location
     * - an attraction
     * - a reward point
     *
     * @param userName the username whose rewards are sought
     * @return a list of user reward
     */
    @GetMapping("/getRewards")
    public ResponseEntity<List<UserReward>> getRewards(@RequestParam String userName) {
        logger.info("REST : Get user rewards");
        try {
            User user = tourGuideService.getUser(userName);
            List<UserReward> userRewards = rewardsService.getUserRewards(user);
            return ResponseEntity.ok(userRewards);
        } catch (UserNotFoundException e) {
            logger.error("REST : " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get a list of every user's most recent location as JSON
     *
     * @return a map with for each user key = userId and value = {latitude, longitude}
     */
    @GetMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        logger.info("REST : Get all current user location");
        return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    /**
     * Get a list of provider with price offer by user.
     * Each provider contains :
     * - a name
     * - a price
     * - an id
     *
     * @param userName the userName whose providers are sought
     * @param attractionName the name of the attraction for which the deals are requested
     * @return a list of providers with price offer
     */
    @GetMapping("/getTripDeals")
    public ResponseEntity<List<Provider>> getTripDeals(@RequestParam String userName, @RequestParam String attractionName) {
        logger.info("REST : Get user trip deal");
        try {
            List<Provider> providers = tripPricerService.getTripDeals(tourGuideService.getUser(userName), tourGuideService.getAttraction(attractionName));
            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            logger.error("REST : " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update user's preferences
     *
     * @param userName the username of user whose preferences are to be updated
     * @param preferencesUpdated the preferences to update
     * @return updated user preferences
     */
    @PutMapping("/updateUserPreferences")
    public ResponseEntity<UserPreferences> updateUserPreferences(@RequestParam String userName, @RequestBody UserPreferences preferencesUpdated) {
        logger.info("REST : Update user preferences");
        try {
            UserPreferences userPreferenceToUpdate = tripPricerService.updateUserPreferences(tourGuideService.getUser(userName), preferencesUpdated);
            return ResponseEntity.ok(userPreferenceToUpdate);
        } catch (UserNotFoundException e) {
            logger.error("REST : " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get a user by userName
     *
     * @param userName of user sought
     * @return the user found
     */
    @GetMapping("/getUser")
    private ResponseEntity<User> getUser(@RequestParam String userName) {
        logger.info("REST : Get user by name");
        try {
            User user = tourGuideService.getUser(userName);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            logger.error("REST : " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    }