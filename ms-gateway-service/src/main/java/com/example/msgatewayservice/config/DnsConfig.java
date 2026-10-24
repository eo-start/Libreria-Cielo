/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msgatewayservice.config;

import java.time.Duration;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DnsConfig {
    @Bean
    HttpClientCustomizer refreshServiceAddresses() {
        return client -> client.resolver(resolver -> resolver
                .cacheMinTimeToLive(Duration.ZERO)
                .cacheMaxTimeToLive(Duration.ofSeconds(5))
                .cacheNegativeTimeToLive(Duration.ZERO));
    }
}
