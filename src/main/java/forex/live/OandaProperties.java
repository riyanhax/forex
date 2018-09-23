package forex.live;

import forex.trader.TraderConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "oanda")
public class OandaProperties {
    public static class Api {
        private String endpoint;
        private String token;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private Api api;
    private List<TraderConfiguration> traders;

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public List<TraderConfiguration> getTraders() {
        return traders;
    }

    public void setTraders(List<TraderConfiguration> traders) {
        this.traders = traders;
    }
}
