package org.horiga.study.webapp.repsository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.horiga.study.webapp.model.TestEntity;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TestRepository {

    @Select("SELECT * FROM test")
    List<TestEntity> all();

    @Select("SELECT * FROM test WHERE id = #{id}")
    Optional<TestEntity> findById(Long id);

    @Insert("INSERT INTO test(name, type) VALUES(#{name},#{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void add(TestEntity entity);
}
