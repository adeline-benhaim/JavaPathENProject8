package tourGuide;

import com.jsoniter.output.JsonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;
    @Autowired
    TripPricerService tripPricerService;

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
    public ResponseEntity<String> getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
        try {
            VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(tourGuideService.getUser(userName));
            return ResponseEntity.ok(JsonStream.serialize(visitedLocation.locationBean));
        } catch (UserNotFoundException e) {
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
    public ResponseEntity<String> getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        try {
            VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(tourGuideService.getUser(userName));
            return ResponseEntity.ok(JsonStream.serialize(tourGuideService.nearbyAttractionListByUserDto(visitedLocation)));
        } catch (UserNotFoundException e) {
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
    public ResponseEntity<String> getRewards(@RequestParam String userName) {
        try {
            return ResponseEntity.ok(JsonStream.serialize(tourGuideService.getUserRewards(tourGuideService.getUser(userName))));
        } catch (UserNotFoundException e) {
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
     * @return a list of providers with price offer
     */
    @GetMapping("/getTripDeals")
    public ResponseEntity<String> getTripDeals(@RequestParam String userName) {
        try {
            List<Provider> providers = tripPricerService.getTripDeals(tourGuideService.getUser(userName));
            return ResponseEntity.ok(JsonStream.serialize(providers));
        } catch (UserNotFoundException e) {
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
        try {
            UserPreferences userPreferenceToUpdate = tripPricerService.updateUserPreferences(tourGuideService.getUser(userName), preferencesUpdated);
            return ResponseEntity.ok(userPreferenceToUpdate);
        } catch (UserNotFoundException e) {
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
    private User getUser(@RequestParam String userName) {
        return tourGuideService.getUser(userName);
    }
}