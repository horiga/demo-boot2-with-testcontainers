package org.horiga.study.webapp.service;

import java.util.Optional;

import org.horiga.study.webapp.model.TestEntity;
import org.horiga.study.webapp.testcontainers.MySqlDockerComposeContextInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = MySqlDockerComposeContextInitializer.class)
public class TestServiceTest {

    @Autowired TestService service;

    @Test
    public void test() {
        // test select with initial csv data
        Assertions.assertFalse(service.entities().isEmpty(), "must not entity is empty");

        final TestEntity newEntity = service.addEntity("horiga", "O");
        Assertions.assertNotNull(newEntity, "added entity must not null");
        Assertions.assertTrue(newEntity.getId() > 0, "must generated auto increment identity");

        final Optional<TestEntity> entity = service.getEntity(newEntity.getId());
        Assertions.assertTrue(entity.isPresent());
        Assertions.assertEquals("horiga", entity.get().getName());
        Assertions.assertEquals("O", entity.get().getType());
        Assertions.assertTrue(service.entities().size() > 2);
    }
}
