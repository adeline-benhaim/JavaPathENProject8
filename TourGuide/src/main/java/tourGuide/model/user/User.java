package tourGuide.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import tourGuide.beans.VisitedLocationBean;
import tripPricer.Provider;

@Getter
@Setter
public class User {
	private final UUID userId;
	private final String userName;
	private String phoneNumber;
	private String emailAddress;
	private Date latestLocationTimestamp;
	private List<VisitedLocationBean> visitedLocations = new ArrayList<>();
	private List<UserReward> userRewards = new ArrayList<>();
	public UserPreferences userPreferences = new UserPreferences();
	private List<Provider> tripDeals = new ArrayList<>();
	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}
	
	public void addToVisitedLocations(VisitedLocationBean visitedLocation) {
		visitedLocations.add(visitedLocation);
	}
	
	public void clearVisitedLocations() {
		visitedLocations.clear();
	}
	
	public void addUserReward(UserReward userReward) {
		if (userRewards.stream().filter(r -> r.attraction.attractionName.equals(userReward
				.attraction.attractionName)).count() == 0) {
			userRewards.add(userReward);
		}
	}

	public VisitedLocationBean getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}

}
