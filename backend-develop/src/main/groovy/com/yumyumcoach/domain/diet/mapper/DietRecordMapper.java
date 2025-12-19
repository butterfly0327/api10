package com.yumyumcoach.domain.diet.mapper;

import com.yumyumcoach.domain.diet.dto.CreateDietRecordRequest;
import com.yumyumcoach.domain.diet.dto.DietRecordDto;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DietRecordMapper {

    int insertDietRecord(
            @Param("email") String email,
            @Param("req") CreateDietRecordRequest req
    );

    int updateDietRecord(
            @Param("id") Long id,
            @Param("email") String email,
            @Param("req") CreateDietRecordRequest req
    );

    int deleteDietRecord(
            @Param("id") Long id,
            @Param("email") String email
    );

    DietRecordDto selectDietRecordDetail(
            @Param("id") Long id,
            @Param("email") String email
    );

    List<DietRecordDto> selectDietRecordsByUser(
            @Param("email") String email,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    List<DietRecordDto> selectDietRecordsByUserAndDate(
            @Param("email") String email,
            @Param("recordDate") LocalDate recordDate,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    boolean existsById(@Param("id") Long id);
    boolean existsByIdAndEmail(@Param("id") Long id, @Param("email") String email);
}

