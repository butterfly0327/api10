package com.yumyumcoach.domain.exercise.mapper;

import com.yumyumcoach.domain.exercise.entity.Exercise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExerciseMapper {
    List<Exercise> findAll();

    Exercise findById(@Param("exerciseId") Long exerciseId);
}
