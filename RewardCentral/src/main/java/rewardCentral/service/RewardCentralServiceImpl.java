package rewardCentral.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.UUID;

@Service
public class RewardCentralServiceImpl implements RewardCentralService {

    private final RewardCentral rewardCentral;

    public RewardCentralServiceImpl(RewardCentral rewardCentral) {
        this.rewardCentral = rewardCentral;
    }

    /**
     * Get points awarded to a user for a given attraction
     * @param attractionId the id of the attraction concerned
     * @param userId the id of the user concerned
     * @return the number of points assigned to the user for this attraction
     */
    @Autowired
    public int getRewardPoints(UUID attractionId, UUID userId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}
