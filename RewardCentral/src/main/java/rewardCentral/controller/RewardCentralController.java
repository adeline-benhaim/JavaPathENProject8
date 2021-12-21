package rewardCentral.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.service.RewardCentralService;

import java.util.UUID;

@RestController
public class RewardCentralController {
    private final Logger logger = LoggerFactory.getLogger(RewardCentralController.class);

    private RewardCentralService rewardCentralService;

    public RewardCentralController(RewardCentralService rewardCentralService){
        this.rewardCentralService = rewardCentralService;
    }

    /**
     * Get points awarded to a user for a given attraction
     * @param attractionId the id of the attraction concerned
     * @param userId the id of the user concerned
     * @return the number of points assigned to the user for this attraction
     */
    @GetMapping("/getRewards")
    public int getRewards(@RequestParam UUID attractionId, @RequestParam UUID userId) {
        logger.info("RewardCentral : Get points awarded to a user id : {} for a given attraction id : {}",userId,attractionId);
        return rewardCentralService.getRewardPoints (attractionId, userId);
    }
}
