package tourGuide;

import com.jsoniter.output.JsonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.model.user.User;
import tourGuide.service.TourGuideService;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

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
    public String getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
        VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.locationBean);
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
    public String getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(tourGuideService.nearbyAttractionListByUserDto(visitedLocation));
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
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    @GetMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        // TODO: Get a list of every user's most recent location as JSON
        //- Note: does not use gpsUtil to query for their current location,
        //        but rather gathers the user's current location from their stored location history.
        //
        // Return object should be the just a JSON mapping of userId to Locations similar to:
        //     {
        //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
        //        ...
        //     }

        return JsonStream.serialize("");
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
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    /**
     * Get a user by userName
     *
     * @param userName of user sought
     * @return the user found
     */
    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }


}