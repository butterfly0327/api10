package com.yumyumcoach.domain.challenge.mapper;

import com.yumyumcoach.domain.challenge.entity.ChallengeRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChallengeRuleMapper {

    /**
     * 챌린지 + 난이도 기준으로 룰 한 건을 조회한다.
     *
     * @param challengeId   챌린지 ID
     * @param difficultyCode 난이도 코드 (BEGINNER / INTERMEDIATE / ADVANCED)
     */
    ChallengeRule findByChallengeIdAndDifficulty(
            @Param("challengeId") Long challengeId,
            @Param("difficultyCode") String difficultyCode
    );
}

