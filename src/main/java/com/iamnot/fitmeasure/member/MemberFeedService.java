package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.measurement.session.MeasurementSession;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.member.dto.FeedItem;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberFeedService {

    private final MembershipRepository membershipRepository;
    private final MeasurementSessionRepository sessionRepository;

    /** 로그인한 회원의 모든 클럽 측정 기록을 최신순 피드로 */
    @Transactional(readOnly = true)
    public List<FeedItem> myFeed(AppPrincipal loginMember) {
        Long memberId = loginMember.memberId();

        // 이 사람의 MEMBER 역할 멤버십 전부 (여러 클럽)
        List<Membership> memberships = membershipRepository
                .findByMemberIdAndRole(memberId, MembershipRole.MEMBER);

        List<FeedItem> feed = new ArrayList<>();
        for (Membership ms : memberships) {
            String clubName = ms.getClub().getName();
            List<MeasurementSession> sessions = sessionRepository
                    .findByMembershipIdOrderByMeasuredAtDesc(ms.getId());
            for (MeasurementSession s : sessions) {
                long measured = s.getValues().stream().filter(v -> !v.isSkipped()).count();
                feed.add(new FeedItem(
                        s.getId(), clubName, s.getTemplate().getName(),
                        s.getMeasuredAt(), (int) measured));
            }
        }
        feed.sort(Comparator.comparing(FeedItem::measuredAt).reversed());
        return feed;
    }
}