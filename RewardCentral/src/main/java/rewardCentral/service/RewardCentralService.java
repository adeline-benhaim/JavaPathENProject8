package rewardCentral.service;

import java.util.UUID;

public interface RewardCentralService {

    /**
     * Get points awarded to a user for a given attraction
     * @param attractionId the id of the attraction concerned
     * @param userId the id of the user concerned
     * @return the number of points assigned to the user for this attraction
     */
    int getRewardPoints(UUID attractionId, UUID userId);
}
