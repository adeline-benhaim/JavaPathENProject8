package gpsUtil.configuration;

import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class GpsUtilModule {

    @Bean
    public GpsUtil getGpsUtil() {
        Locale.setDefault(new Locale("en", "US"));
        return new GpsUtil();
    }
}
