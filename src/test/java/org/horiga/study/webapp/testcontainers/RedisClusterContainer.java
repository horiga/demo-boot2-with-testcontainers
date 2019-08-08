/*
 * Copyright (c) 2019 LINE Corporation. All rights reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.horiga.study.webapp.testcontainers;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

public class RedisClusterContainer extends GenericContainer<RedisClusterContainer> {

    @SuppressWarnings("FieldMayBeFinal")
    private static Logger log = LoggerFactory.getLogger(RedisClusterContainer.class);

    public static final Integer CLUSTER_NODES = 6;

    public static final String ENV_REDIS_CLUSTER_PORTS = "REDIS_CLUSTER_PORTS";

    public static final String DEFAULT_DOCKER_IMAGE = "quay.io/horiga/redis-cluster:4.0.10";

    public static WaitStrategy waitStrategy = Wait
            .forLogMessage(".*Redis cluster is already accepted connections.*", 1);

    private final List<Integer> ports;

    public RedisClusterContainer() {
        this(DEFAULT_DOCKER_IMAGE, true);
    }

    public RedisClusterContainer(final String dockerImageName, boolean withStarting) {
        super(dockerImageName);
        ports = generateBindingPorts();
        if (withStarting) {
            startContainer();
        }
    }

    public void startContainer() {
        if (isRunning()) {
            log.warn("Already container running. container={}({})", containerId, containerName);
            return;
        }
        setPortBindings(ports.stream().map(p -> p + ":" + p).collect(Collectors.toList()));
        // noinspection ZeroLengthArrayAllocation
        withExposedPorts(ports.toArray(new Integer[0]));
        final StringJoiner stringJoiner = new StringJoiner(" ");
        ports.forEach(p -> stringJoiner.add(p.toString()));
        withEnv(ENV_REDIS_CLUSTER_PORTS, stringJoiner.toString());
        waitingFor(waitStrategy);
        withCommand("yes", "Y");
        start();
        log.info("Starting docker container. {}({})", containerId, containerName);
    }

    public List<Integer> getBindingPorts() {
        return Collections.unmodifiableList(ports);
    }

    @SuppressWarnings("MethodMayBeStatic")
    private List<Integer> generateBindingPorts() {
        // Port range is 6379 - 58621, a.k.a 6379,6380,6381,6382,6383,6384 ~ 58621,58622,58623,58624,58625,58626
        final int startPort = new Random(Instant.now().toEpochMilli()).nextInt(58621) + 6379;
        return IntStream.range(startPort, startPort + CLUSTER_NODES).boxed().collect(Collectors.toList());
    }
}
