package com.yumyumcoach.domain.user.service;

import com.yumyumcoach.domain.auth.entity.Account;
import com.yumyumcoach.domain.auth.mapper.AccountMapper;
import com.yumyumcoach.domain.user.dto.MyTitleResponse;
import com.yumyumcoach.domain.user.dto.MyPageResponse;
import com.yumyumcoach.domain.user.dto.UpdateMyBasicInfoRequest;
import com.yumyumcoach.domain.user.dto.UpdateMyHealthInfoRequest;
import com.yumyumcoach.domain.user.entity.Profile;
import com.yumyumcoach.domain.user.mapper.FollowMapper;
import com.yumyumcoach.domain.user.mapper.ProfileMapper;
import com.yumyumcoach.domain.user.mapper.UserTitleMapper;
import com.yumyumcoach.global.common.CdnUrlResolver;
import com.yumyumcoach.global.exception.BusinessException;
import com.yumyumcoach.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final AccountMapper accountMapper;
    private final ProfileMapper profileMapper;
    private final FollowMapper followMapper;
    private final UserTitleMapper userTitleMapper;
    private final CdnUrlResolver cdnUrlResolver;

    public MyPageResponse getMyPage(String email) {

        Account account = accountMapper.findByEmail(email);
        if (account == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Long userId = accountMapper.findIdByEmail(email);

        Profile profile = profileMapper.findByEmail(email);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }

        long followers = followMapper.countFollowers(email);
        long followings = followMapper.countFollowings(email);

        MyTitleResponse current = userTitleMapper.findCurrentTitle(email);
        List<MyPageResponse.TitleItem> myTitles = userTitleMapper.findMyTitles(email);

        return MyPageResponse.builder()
                .basic(MyPageResponse.Basic.builder()
                        .userId(userId)
                        .email(email)
                        .username(account.getUsername())
                        .profileImageUrl(cdnUrlResolver.resolve(profile.getProfileImageUrl()))
                        .introduction(profile.getIntroduction())
                        .build())
                .health(MyPageResponse.Health.builder()
                        .birthDate(profile.getBirthDate())
                        .height(profile.getHeight())
                        .weight(profile.getCurrentWeight())
                        .goalWeight(profile.getTargetWeight())
                        .hasDiabetes(profile.getHasDiabetes())
                        .hasHypertension(profile.getHasHypertension())
                        .hasHyperlipidemia(profile.getHasHyperlipidemia())
                        .otherDisease(profile.getOtherDisease())
                        .goal(profile.getGoal())
                        .activityLevel(profile.getActivityLevel())
                        .build())
                .badges(MyPageResponse.Badges.builder()
                        .currentTitleId(current.getCurrentTitleId())
                        .currentTitleName(current.getCurrentTitleName())
                        .titles(myTitles)
                        .build())
                .follow(MyPageResponse.Follow.builder()
                        .followersCount(followers)
                        .followingsCount(followings)
                        .build())
                .build();
    }

    @Transactional
    public MyPageResponse.Basic updateMyBasicInfo(String email, UpdateMyBasicInfoRequest req) {

        if (req == null || !req.hasAnyValue()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        Profile profile = profileMapper.findByEmail(email);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }

        // TODO: 닉네임 업데이트 로직 구현하기

        Profile patch = Profile.builder()
                .email(email)
                .profileImageUrl(req.getProfileImageUrl())
                .introduction(req.getIntroduction())
                .build();

        profileMapper.updateBasic(patch);

        Profile updated = profileMapper.findByEmail(email);
        Long userId = accountMapper.findIdByEmail(email);
        Account account = accountMapper.findByEmail(email);

        return MyPageResponse.Basic.builder()
                .userId(userId)
                .email(email)
                .username(account.getUsername())
                .profileImageUrl(cdnUrlResolver.resolve(updated.getProfileImageUrl()))
                .introduction(updated.getIntroduction())
                .build();
    }

    @Transactional
    public MyPageResponse.Health updateMyHealthInfo(String email, UpdateMyHealthInfoRequest req) {
        if (req == null || !req.hasAnyValue()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        Profile profile = profileMapper.findByEmail(email);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }

        Profile patch = Profile.builder()
                .email(email)
                .birthDate(req.getBirthDate())
                .height(req.getHeight())
                .currentWeight(req.getWeight())
                .targetWeight(req.getGoalWeight())
                .hasDiabetes(req.getHasDiabetes())
                .hasHypertension(req.getHasHypertension())
                .hasHyperlipidemia(req.getHasHyperlipidemia())
                .otherDisease(req.getOtherDisease())
                .goal(req.getGoal())
                .activityLevel(req.getActivityLevel())
                .build();

        profileMapper.updateHealth(patch);

        Profile updated = profileMapper.findByEmail(email);

        return MyPageResponse.Health.builder()
                .birthDate(updated.getBirthDate())
                .height(updated.getHeight())
                .weight(updated.getCurrentWeight())
                .goalWeight(updated.getTargetWeight())
                .hasDiabetes(updated.getHasDiabetes())
                .hasHypertension(updated.getHasHypertension())
                .hasHyperlipidemia(updated.getHasHyperlipidemia())
                .otherDisease(updated.getOtherDisease())
                .goal(updated.getGoal())
                .activityLevel(updated.getActivityLevel())
                .build();
    }

    @Transactional
    public MyTitleResponse selectMyTitle(String email, Long titleId) {

        if (!userTitleMapper.ownsTitle(email, titleId)) {
            throw new BusinessException(ErrorCode.USER_TITLE_NOT_FOUND);
        }

        int updated = profileMapper.updateDisplayTitle(email, titleId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }

        return userTitleMapper.findCurrentTitle(email);
    }
}
