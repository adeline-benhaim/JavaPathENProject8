package rewardCentral.configuration;

import org.springframework.context.annotation.Bean;
import rewardCentral.RewardCentral;

import java.util.Locale;

public class RewardCentralModule {

    @Bean
    public RewardCentral getRewardCentral() {
        Locale.setDefault(new Locale("en", "US"));
        return new RewardCentral();
    }
}
