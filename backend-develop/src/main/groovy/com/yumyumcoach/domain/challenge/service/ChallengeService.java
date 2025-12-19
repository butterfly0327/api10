package com.yumyumcoach.domain.challenge.service;

import com.yumyumcoach.domain.challenge.dto.*;
import com.yumyumcoach.domain.challenge.entity.Challenge;
import com.yumyumcoach.domain.challenge.entity.ChallengeParticipant;
import com.yumyumcoach.domain.challenge.mapper.ChallengeMapper;
import com.yumyumcoach.domain.challenge.mapper.ChallengeParticipantMapper;
import com.yumyumcoach.domain.challenge.model.DifficultyCode;
import com.yumyumcoach.domain.challenge.model.GoalType;
import com.yumyumcoach.global.exception.BusinessException;
import com.yumyumcoach.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 공용 챌린지 도메인 서비스.
 * 챌린지 목록/상세 조회, 참여/나가기 등을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {
    private final ChallengeMapper challengeMapper;
    private final ChallengeParticipantMapper challengeParticipantMapper;
    private final ChallengeRuleResolver challengeRuleResolver;

    /**
     * 특정 월 기준 챌린지 목록을 조회한다.
     *
     * @param month yyyy-MM 형식의 조회 기준 월
     * @param email 현재 로그인한 사용자 이메일
     */
    public ChallengeListResponse getChallenges(String month, String email) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 이번 달(월말인 경우 다음 달 포함) 챌린지 조회
        List<Challenge> challenges = challengeMapper.findByPeriod(startDate, endDate);

        List<ChallengeResponse> challengeResponses = challenges.stream()
                .map(challenge -> toChallengeResponseForList(challenge, email))
                .toList();

        return ChallengeListResponse.builder()
                .month(month)
                .challenges(challengeResponses)
                .build();
    }

    /**
     * 챌린지 상세 정보를 조회한다.
     *
     * @param challengeId 챌린지 ID
     * @param email       현재 로그인한 사용자 이메일
     */
    public ChallengeResponse getChallengeDetail(Long challengeId, String email) {
        Challenge challenge = challengeMapper.findById(challengeId);
        if (challenge == null) {
            throw new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND);
        }

        ChallengeParticipant participant = challengeParticipantMapper.findByChallengeIdAndEmail(challengeId, email);
        return toChallengeResponseForDetail(challenge, participant);
    }

    /**
     * 챌린지에 참여(사전 신청 포함)한다.
     * - challenge_participants 에 어떤 형태로든 참여 이력이 있으면 재참여를 허용하지 않는다.
     * @param challengeId 챌린지 ID
     * @param email       현재 로그인한 사용자 이메일
     * @param request     참여 요청 (난이도 포함)
     */
    @Transactional
    public JoinChallengeResponse joinChallenge(Long challengeId, String email, JoinChallengeRequest request) {
        // 1) 챌린지 존재 여부 확인
        Challenge challenge = challengeMapper.findById(challengeId);
        if (challenge == null) {
            throw new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND);
        }

        // 2) 이미 참여 중인지 체크
        int existing = challengeParticipantMapper.existsByChallengeIdAndEmail(challengeId, email);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.CHALLENGE_ALREADY_JOINED);
        }

        // 3) 난이도 및 목표 타입 결정
        DifficultyCode difficultyCode = DifficultyCode.from(request.getDifficultyCode());
        GoalType goalType = GoalType.from(challenge.getGoalType());

        // 4) 챌린지 룰 조회
        int requiredSuccessDays = challengeRuleResolver.resolveRequiredSuccessDays(challenge, difficultyCode);
        Double dailyTargetValue = challengeRuleResolver.resolveDailyTargetValue(challenge, goalType, difficultyCode);

        // 5) 참여 엔티티 생성 및 저장
        ChallengeParticipant participant = ChallengeParticipant.newJoin(
                challengeId,
                email,
                difficultyCode.getCode(),
                requiredSuccessDays,
                dailyTargetValue
        );

        // 6) DB에 저장
        challengeParticipantMapper.insert(participant);

        LocalDate myStartDate = challenge.getStartDate();
        LocalDate myEndDate = challenge.getEndDate();

        // 7) 응답 DTO 생성
        return JoinChallengeResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getName())
                .joined(true)
                .joinedAt(participant.getJoinedAt().toString())
                .difficultyCode(difficultyCode.getCode())
                .requiredSuccessDays(requiredSuccessDays)
                .dailyTargetValue(dailyTargetValue)
                .myStartDate(myStartDate.toString())
                .myEndDate(myEndDate.toString())
                .build();
    }

    /**
     * 챌린지를 나간다.
     * - today < startDate : 사전 신청 취소 → row DELETE
     * - today >= startDate : 중도 탈퇴 → status = 'left' 로 변경
     * - 이미 status = 'left' 인 경우 → "이미 나간 챌린지입니다." 에러
     * @param challengeId 챌린지 ID
     * @param email       현재 로그인한 사용자 이메일
     */
    @Transactional
    public LeaveChallengeResponse leaveChallenge(Long challengeId, String email) {
        // 1) 챌린지 존재 여부 확인
        Challenge challenge = challengeMapper.findById(challengeId);
        if (challenge == null) {
            throw new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND);
        }

        // 2) 참여중이지 않은지 체크
        ChallengeParticipant existing = challengeParticipantMapper.findByChallengeIdAndEmail(challengeId, email);
        if (existing == null) {
            throw new BusinessException(ErrorCode.CHALLENGE_JOIN_NOT_FOUND);
        }
        if ("left".equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(ErrorCode.CHALLENGE_ALREADY_LEFT);
        }

        // 3) 챌린지 시작 전 -> 사전 신청 취소 -> row 삭제 / 챌린지 시작 후 → 중도 탈퇴 → status = 'left' 로 변경
        LocalDate today = LocalDate.now();
        boolean isBeforeStart = today.isBefore(challenge.getStartDate());
        LocalDateTime leftAt = LocalDateTime.now();
        if (isBeforeStart) {
            challengeParticipantMapper.deleteByChallengeIdAndEmail(challengeId, email);
        } else {
            existing.leave(leftAt);
            challengeParticipantMapper.updateStatus(challengeId, email, "left", leftAt);
        }

        // 4) 응답 DTO 생성
        return LeaveChallengeResponse.builder()
                .challengeId(challengeId)
                .left(true)
                .leftAt(leftAt.toString())
                .build();
    }

    /**
     * 목록 조회용 ChallengeResponse 변환.
     * - ruleDescription 등 상세 화면에서만 필요한 값은 null 로 둔다.
     */
    private ChallengeResponse toChallengeResponseForList(Challenge challenge, String email) {
        ChallengeParticipant participant =
                challengeParticipantMapper.findByChallengeIdAndEmail(challenge.getId(), email);

        int participantsCount = challengeParticipantMapper.countByChallengeId(challenge.getId());

        Integer successDays = null;
        Double progressPercentage = null;
        String selectedDifficulty = null;
        Integer requiredSuccessDays = null;
        Double dailyTargetValue = null;
        boolean isJoined = false;

        if (participant != null && !"left".equalsIgnoreCase(participant.getStatus())) {
            isJoined = true;
            successDays = participant.getSuccessDays();
            progressPercentage = participant.getProgressPercentage();
            selectedDifficulty = participant.getDifficultyCode();
            requiredSuccessDays = participant.getRequiredSuccessDays();
            dailyTargetValue = participant.getDailyTargetValue();
        }

        return ChallengeResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getName())
                .shortDescription(challenge.getShortDescription())
                .goalSummary(challenge.getGoalSummary())
                .ruleDescription(null) // 목록에서는 유의 사항은 내려주지 않음
                .imageUrl(challenge.getImageUrl())
                .type(challenge.getChallengeType())
                .goalType(challenge.getGoalType())
                .startDate(challenge.getStartDate().toString())
                .endDate(challenge.getEndDate().toString())
                .participantsCount(participantsCount)
                .isJoined(isJoined)
                .selectedDifficulty(selectedDifficulty)
                .requiredSuccessDays(requiredSuccessDays)
                .dailyTargetValue(dailyTargetValue)
                .successDays(successDays)
                .progressPercentage(progressPercentage)
                .build();
    }

    /**
     * 상세 조회용 ChallengeResponse 변환.
     * - ruleDescription 포함, 참여 정보도 함께 내려준다.
     */
    private ChallengeResponse toChallengeResponseForDetail(Challenge challenge,
                                                           ChallengeParticipant participant) {
        int participantsCount = challengeParticipantMapper.countByChallengeId(challenge.getId());

        Integer successDays = null;
        Double progressPercentage = null;
        String selectedDifficulty = null;
        Integer requiredSuccessDays = null;
        Double dailyTargetValue = null;
        boolean isJoined = false;

        if (participant != null && !"left".equalsIgnoreCase(participant.getStatus())) {
            isJoined = true;
            successDays = participant.getSuccessDays();
            progressPercentage = participant.getProgressPercentage();
            selectedDifficulty = participant.getDifficultyCode();
            requiredSuccessDays = participant.getRequiredSuccessDays();
            dailyTargetValue = participant.getDailyTargetValue();
        }

        return ChallengeResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getName())
                .shortDescription(challenge.getShortDescription())
                .goalSummary(challenge.getGoalSummary())
                .ruleDescription(challenge.getRuleDescription())
                .imageUrl(challenge.getImageUrl())
                .type(challenge.getChallengeType())
                .goalType(challenge.getGoalType())
                .startDate(challenge.getStartDate().toString())
                .endDate(challenge.getEndDate().toString())
                .participantsCount(participantsCount)
                .isJoined(isJoined)
                .selectedDifficulty(selectedDifficulty)
                .requiredSuccessDays(requiredSuccessDays)
                .dailyTargetValue(dailyTargetValue)
                .successDays(successDays)
                .progressPercentage(progressPercentage)
                .build();
    }
}
