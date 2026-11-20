package cl.pagoflex.compromisos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ServiceClientConfig {
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder(
            @Value("${services.internal-token:}") String internalToken,
            @Value("${services.connect-timeout-ms:2000}") long connectTimeout,
            @Value("${services.read-timeout-ms:3000}") long readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Token", internalToken);
    }
}
