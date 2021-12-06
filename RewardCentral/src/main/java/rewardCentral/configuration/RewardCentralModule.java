package rewardCentral.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rewardCentral.RewardCentral;

import java.util.Locale;

@Configuration
public class RewardCentralModule {

    @Bean
    public RewardCentral getRewardCentral() {
        Locale.setDefault(new Locale("en", "US"));
        return new RewardCentral();
    }
}
