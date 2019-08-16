package org.horiga.study.webapp.config;

import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger;

@SuppressWarnings("FieldMayBeFinal")
@Configuration
public class ApplicationConfig {

    private static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties properties) {

        StringBuilder sb = new StringBuilder()
                .append("\n===========================")
                .append("\nspring.redis.cluster.nodes=")
                .append(properties.getCluster().getNodes())
                .append("\nspring.redis.cluster.max-redirects=")
                .append(properties.getCluster().getMaxRedirects())
                .append("\nspring.redis.timeout=").append(properties.getTimeout())
                .append("\n===========================");

        log.info(sb.toString());

        final RedisClusterConfiguration clusterConfiguration =
                new RedisClusterConfiguration(properties.getCluster().getNodes());

        if (!Objects.isNull(properties.getCluster().getMaxRedirects())) {
            // max-redirects
            clusterConfiguration.setMaxRedirects(properties.getCluster().getMaxRedirects());
        }

        if (!StringUtils.isEmpty(properties.getPassword())) {
            clusterConfiguration.setPassword(RedisPassword.of(properties.getPassword()));
        }

        final ClusterClientOptions clientOptions =
                ClusterClientOptions.builder()
                                    // .pingBeforeActivateConnection(true)
                                    .socketOptions(SocketOptions.builder()
                                                                .connectTimeout(Duration.ofMillis(5000))
                                                                .tcpNoDelay(true)
                                                                .keepAlive(true)
                                                                .build())
                                    .topologyRefreshOptions(
                                            ClusterTopologyRefreshOptions
                                                    .builder()
                                                    .enablePeriodicRefresh(Duration.ofMinutes(5))
                                                    .enableAdaptiveRefreshTrigger(RefreshTrigger.MOVED_REDIRECT,
                                                                                  RefreshTrigger.ASK_REDIRECT,
                                                                                  RefreshTrigger.PERSISTENT_RECONNECTS)
                                                    .enableAllAdaptiveRefreshTriggers()
                                                    .build())
                                    // 'false': Avoid failure when added the new membership cluster node.
                                    .validateClusterNodeMembership(false)
                                    .build();

        final LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(clusterConfiguration,
                                             LettuceClientConfiguration.builder()
                                                                       .commandTimeout(Duration.ofMillis(3000))
                                                                       .clientOptions(clientOptions)
                                                                       .build());
        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }
}
