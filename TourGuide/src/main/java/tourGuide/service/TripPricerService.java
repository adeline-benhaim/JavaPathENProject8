package tourGuide.service;

import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tripPricer.Provider;

import java.util.List;

public interface TripPricerService {

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
     * Update user's preferences
     *
     * @param user the user whose preferences are to be updated
     * @param userPreferences the preferences to update
     * @return updated user preferences
     */
    UserPreferences updateUserPreferences(User user, UserPreferences userPreferences);
}
