package com.gongkademy.domain.member.service;

import com.gongkademy.domain.member.dto.MemberInfoDTO;
import com.gongkademy.domain.member.dto.MemberSignUpDTO;
import com.gongkademy.domain.member.dto.MemberUpdateDTO;
import com.gongkademy.domain.member.entity.Member;
import com.gongkademy.domain.member.entity.MemberRole;
import com.gongkademy.domain.member.repository.MemberRepository;
import com.gongkademy.global.security.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;
    private final String DELETE_NICKNAME = "탈퇴회원";

    /**
     * 주어진 회원 ID로 회원 정보를 가져옵니다.
     * @param id 회원 ID
     * @return 회원 정보 DTO
     */
    @Override
    public MemberInfoDTO getMemberInfo(long id) {
        Member member = memberRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        //TODO: 회원 못찾으면 예외처리
        return entityToMemberInfoDTO(member);
    }

    /**
     * @param id 회원 ID
     * @param memberSignUpDTO 회원가입 정보
     * @return 회원 ID
     */
    @Override
    public Long joinMember(long id, MemberSignUpDTO memberSignUpDTO) {
        Optional<Member> optMember = memberRepository.findById(id);

        if (optMember.isEmpty()) return null;

        Member member = optMember.get();
        member.addRole(MemberRole.USER);
        member.signup(memberSignUpDTO);

        String refreshToken = jwtUtil.createRefreshToken(member.getId());
        jwtUtil.setRefreshToken(member.getId(), refreshToken);

        return member.getId();
    }

    /**
     * @param id 회원 ID
     * @param memberUpdateDTO 회원수정 정보
     * @return 회원 ID
     */
    @Override
    public Long modifyMember(long id, MemberUpdateDTO memberUpdateDTO) {
        Optional<Member> optMember = memberRepository.findById(id);

        if (optMember.isEmpty()) return null;

        Member member = optMember.get();
        member.update(memberUpdateDTO);

        return member.getId();
    }

    /**
     * 실제 삭제가 아닌 soft-delete를 구현
     * @param id memberId
     * @return memberId
     */
    @Override
    public Long deleteMember(long id) {
        Optional<Member> memberOptional = memberRepository.findById(id);
        Member member;
        if (memberOptional.isPresent()) member = memberOptional.get();
        else return null;

        // 닉네임 -> 탈퇴회원
        // 탈퇴여부 -> true
        // 탈퇴시간 -> now()
        member.deleteMember(DELETE_NICKNAME + member.getId());
        log.info("탈퇴후 멤버의 닉네임 : "+ member.getNickname());
        return member.getId();
    }

    @Override
    public Long changeNotificationEnabledStatus(long id) {
        Optional<Member> memberOptional = memberRepository.findById(id);
        if (memberOptional.isEmpty()) return null;

        Member member = memberOptional.get();
        member.changeIsNotificationEnabled();
        return member.getId();
    }


}
