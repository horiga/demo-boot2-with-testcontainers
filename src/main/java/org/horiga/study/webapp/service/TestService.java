package org.horiga.study.webapp.service;

import java.util.List;
import java.util.Optional;

import org.horiga.study.webapp.model.TestEntity;
import org.horiga.study.webapp.repsository.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
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
}
