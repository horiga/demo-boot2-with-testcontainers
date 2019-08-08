/*
 * Copyright (c) 2019 LINE Corporation. All rights reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.horiga.study.webapp.testcontainers;

import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import lombok.Data;

public class RedisClusterDockerContainerContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @SuppressWarnings("FieldMayBeFinal")
    private static Logger log = LoggerFactory.getLogger(RedisClusterDockerContainerContextInitializer.class);

    @Data
    public static class RedisClusterDockerContainerTestProperties {

        private String imageName;

        public static RedisClusterDockerContainerTestProperties fromEnvironment(ConfigurableEnvironment env) {
            final RedisClusterDockerContainerTestProperties properties =
                    new RedisClusterDockerContainerTestProperties();
            properties.setImageName(env.getProperty("test.docker.redis.image",
                                                    RedisClusterContainer.DEFAULT_DOCKER_IMAGE));
            return properties;
        }
    }

    private RedisClusterContainer container;

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        final RedisClusterDockerContainerTestProperties properties =
                RedisClusterDockerContainerTestProperties.fromEnvironment(context.getEnvironment());
        try {
            container = new RedisClusterContainer(properties.imageName, false);
            container.startContainer();
            final String nodes = getClusterNodes();
            log.info(
                    "\n------------------------------------------\n"
                    + "spring.redis.cluster.nodes={}\n"
                    + "------------------------------------------", nodes);
            TestPropertyValues.of("spring.redis.cluster.nodes=" + nodes).applyTo(context);
        } catch (Exception e) {
            log.error("Failed to startup docker container.", e);
            throw new IllegalStateException("Failed to startup redis-cluster docker container", e);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (!Objects.isNull(container)) {
                        log.info("shutdown docker container. {}", container.getContainerInfo());
                        container.stop();
                    }
                } catch (Exception e) {
                    log.warn("failed to shutdown docker container. {}", container.getContainerInfo());
                }
            }));
        }
    }

    public String getClusterNodes() {
        return Objects.isNull(container) ? "" :
               container.getBindingPorts().stream()
                        .map(p -> "localhost:" + p)
                        .collect(Collectors.joining(","));
    }
}
