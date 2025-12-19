package com.yumyumcoach.domain.diet.service;

import com.yumyumcoach.domain.diet.dto.CreateDietRecordRequest;
import com.yumyumcoach.domain.diet.dto.DietRecordDto;
import com.yumyumcoach.domain.diet.mapper.DietFoodMapper;
import com.yumyumcoach.domain.diet.mapper.DietRecordMapper;
import java.time.LocalDate;
import java.util.List;

import com.yumyumcoach.global.exception.BusinessException;
import com.yumyumcoach.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DietRecordService {

    private final DietRecordMapper dietRecordMapper;
    private final DietFoodMapper dietFoodMapper;

    public DietRecordService(
            DietRecordMapper dietRecordMapper,
            DietFoodMapper dietFoodMapper
    ) {
        this.dietRecordMapper = dietRecordMapper;
        this.dietFoodMapper = dietFoodMapper;
    }

    @Transactional(readOnly = true)
    public List<DietRecordDto> getMyDiets(String email, LocalDate date, int page, int size) {
        int limit = Math.max(size, 1);
        int offset = Math.max(page, 0) * limit;
        return dietRecordMapper.selectDietRecordsByUserAndDate(email, date, offset, limit);
    }

    @Transactional(readOnly = true)
    public DietRecordDto getMyDietDetail(String email, Long dietId) {
        return dietRecordMapper.selectDietRecordDetail(dietId, email);
    }

    @Transactional
    public Long createMyDiet(String email, CreateDietRecordRequest request) {
        dietRecordMapper.insertDietRecord(email, request);
        Long dietId = request.getId();
        if (dietId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "식단 ID 생성에 실패했습니다.");
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            dietFoodMapper.insertDietFoods(dietId, request.getItems());
        }
        return dietId;
    }

    @Transactional
    public void deleteMyDiet(String email, Long dietId) {
        // 먼저 자식 삭제
        dietFoodMapper.deleteDietFoodsByDietId(dietId);
        int deleted = dietRecordMapper.deleteDietRecord(dietId, email);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.DIET_NOT_FOUND);
        }
    }

    @Transactional
    public void updateMyDiet(String email, Long dietId, CreateDietRecordRequest request) {
        int updated = dietRecordMapper.updateDietRecord(dietId, email, request);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.DIET_NOT_FOUND);
        }
        // 음식 항목 재구성: 일단 모두 삭제 후 재삽입 (간단 전략)
        dietFoodMapper.deleteDietFoodsByDietId(dietId);
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            dietFoodMapper.insertDietFoods(dietId, request.getItems());
        }
    }
}

