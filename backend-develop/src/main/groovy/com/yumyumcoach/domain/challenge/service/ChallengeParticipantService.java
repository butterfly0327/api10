package com.yumyumcoach.domain.challenge.service;

import com.yumyumcoach.domain.challenge.entity.Challenge;
import com.yumyumcoach.domain.challenge.entity.ChallengeParticipant;
import com.yumyumcoach.domain.challenge.mapper.ChallengeMapper;
import com.yumyumcoach.domain.challenge.mapper.ChallengeParticipantMapper;
import com.yumyumcoach.domain.challenge.model.GoalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 챌린지 참여자 관련 비즈니스 로직을 담당하는 서비스.
 * 진행률 / 성공 일수 평가 (successDays, progressPercentage 갱신)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeParticipantService {
    private final ChallengeMapper challengeMapper;
    private final ChallengeParticipantMapper challengeParticipantMapper;

    /**
     * 특정 챌린지에 참여 중인 한 명의 사용자의 진행률을 재평가한다.
     * TODO: successDays 계산 쿼리/로직 구현하기
     *
     * @param challengeId 챌린지 ID
     * @param email       사용자 이메일
     */
    @Transactional
    public void evaluateProgress(Long challengeId, String email) {
        // 1) 챌린지 / 참여 정보 조회
        Challenge challenge = challengeMapper.findById(challengeId);
        if (challenge == null) {
            // TODO: 커스텀 예외로 교체
            throw new IllegalArgumentException("존재하지 않는 챌린지입니다. id=" + challengeId);
        }

        ChallengeParticipant participant = challengeParticipantMapper.findByChallengeIdAndEmail(challengeId, email);
        if (participant == null) {
            // TODO: 커스텀 예외로 교체
            throw new IllegalStateException("참여 이력이 없는 챌린지입니다.");
        }

        // 2) 목표 타입에 따라 분기 (실제 쿼리/계산은 다음 이슈에서 구현)
        GoalType goalType = GoalType.from(challenge.getGoalType());

        // TODO: goalType / participant 정보에 따라 successDays / progressPercentage 계산
        // int successDays = ...
        // double progress = ...

        // 지금은 껍데기만, 실제 값은 다음 이슈에서 구현
        // 일단 0으로 초기화 예시만 넣어둔다.
        int successDays = participant.getSuccessDays() != null ? participant.getSuccessDays() : 0;
        double progress = participant.getProgressPercentage() != null
                ? participant.getProgressPercentage()
                : 0.0;

        LocalDateTime now = LocalDateTime.now();

        // 3) 엔티티에 반영
        participant.updateProgress(successDays, progress, now);

        // 4. DB 반영
        // 현재 Mapper 에는 successDays 까지 함께 업데이트하는 메서드가 없으므로
        // 이 부분은 다음 이슈에서 함께 정의할 예정.
        //
        // 예시:
        // challengeParticipantMapper.updateProgressAndSuccessDays(
        //         challengeId,
        //         email,
        //         successDays,
        //         progress,
        //         now
        // );
    }
}

