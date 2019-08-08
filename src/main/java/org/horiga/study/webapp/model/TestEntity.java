package org.horiga.study.webapp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestEntity {
    private Long id;
    private String name;
    private String type;
}
