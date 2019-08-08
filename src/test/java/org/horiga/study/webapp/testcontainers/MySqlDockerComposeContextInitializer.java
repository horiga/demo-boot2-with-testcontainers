package org.horiga.study.webapp.testcontainers;

import java.nio.file.Paths;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import lombok.Data;

public class MySqlDockerComposeContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(MySqlDockerComposeContextInitializer.class);

    private static final WaitStrategy MYSQL_WAIT_STRATEGY = Wait.forLogMessage(".*ready for connections.*\\s",
                                                                               2);

    @SuppressWarnings("rawtypes")
    private DockerComposeContainer container;

    private MysqlDockerComposeTestProperties properties;

    @Data
    public static class MysqlDockerComposeTestProperties {
        private String dockerComposeFile = "../docker/docker-compose.mysql.test.yml";
        private String serviceName = "mysql";
        private String database = "test";
        private String option = "characterEncoding=UTF-8&useSSL=false";
        private Integer port = 3306;

        public static MysqlDockerComposeTestProperties fromEnvironment(ConfigurableEnvironment env) {
            final MysqlDockerComposeTestProperties properties = new MysqlDockerComposeTestProperties();
            properties.setDockerComposeFile(env.getProperty("test.docker.mysql.docker-compose-file",
                                                            "../docker/docker-compose.mysql.test.yml"));
            properties.setServiceName(env.getProperty("test.docker.mysql.serviceName", "mysql"));
            properties.setDatabase(env.getProperty("test.docker.mysql.database", "test"));
            properties.setOption(
                    env.getProperty("test.docker.mysql.option", "characterEncoding=UTF-8&useSSL=false"));
            properties.setPort(Integer.parseInt(env.getProperty("test.docker.mysql.port", "3306")));
            return properties;
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            properties = MysqlDockerComposeTestProperties.fromEnvironment(applicationContext.getEnvironment());
            container = new DockerComposeContainer(Paths.get(properties.dockerComposeFile).toAbsolutePath()
                                                        .normalize().toFile())
                    .withExposedService(properties.serviceName,
                                        properties.port,
                                        MYSQL_WAIT_STRATEGY)
                    .withTailChildContainers(true);
            container.start();
            final String dsConnectUrl = getJdbcConnectionUrl();
            log.info(
                    "\n------------------------------------------\n"
                    + "spring.datasource.url={}\n"
                    + "------------------------------------------",
                    dsConnectUrl);
            TestPropertyValues.of("spring.datasource.url=" + dsConnectUrl).applyTo(applicationContext);
        } catch (Exception e) {
            log.error("Failed to startup docker container.", e);
            throw new IllegalStateException("Failed to startup mysql docker container", e);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (!Objects.isNull(container)) {
                        log.info("shutdown mysql docker container");
                        container.stop();
                    }
                } catch (Exception e) {
                    log.warn("failed to shutdown mysql container");
                }
            }));
        }
    }

    public String getJdbcConnectionUrl() {
        if (Objects.isNull(container)) {
            return "";
        }
        return "jdbc:mysql://" + "localhost:"
               + container.getServicePort(properties.serviceName, properties.port)
               + '/' + properties.database
               + (Strings.isNotBlank(properties.option) ? '?' + properties.option : "");
    }
}
