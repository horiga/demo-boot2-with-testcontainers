package org.horiga.study.webapp.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.horiga.study.webapp.model.TestEntity;
import org.horiga.study.webapp.repsository.TestRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestService {

    private final TestRepository testRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public TestService(TestRepository testRepository, RedisTemplate<String, String> redisTemplate) {
        this.testRepository = testRepository;
        this.redisTemplate = redisTemplate;
    }

    public Optional<TestEntity> getEntity(Long id) {
        return testRepository.findById(id);
    }

    public List<TestEntity> entities() {
        return testRepository.all();
    }

    public TestEntity addEntity(String name, String type) {
        final TestEntity entity = TestEntity.builder()
                                            .name(name)
                                            .type(type).build();
        testRepository.add(entity);
        return entity;
    }

    public Optional<String> getValue(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, 3000, TimeUnit.MILLISECONDS);
    }
}
