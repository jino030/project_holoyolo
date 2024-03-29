package com.holoyolo.app.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.holoyolo.app.member.service.MemberVO;

import lombok.Data;

//시큐리티가 /login 주소 요청이 오면 인터샙트 => 로그인을 진행
//로그인 진행이 완료가되면 시큐리티 session을 만들어준다. => Security ContextHolder에 저장
//session에 들어갈 수 있는 정보 : 객체가 정해져있음, Authentication 타입의 객체를 허용
//Authentication 객체에는 사용자 정보가 있어야함. : 객체타입은 UserDetails만 가능

//Security session => Authentication <= UserDetails(PrincipalDetails)

@Data
public class PrincipalDetails implements UserDetails, OAuth2User{
	
	private MemberVO memberVO; // 콤포지션, getMemberVO가능..
	private Map<String, Object> attributes; // oauth 로그인 정보를 저장..
	
	// 일반로그인 생성자
	public PrincipalDetails(MemberVO memberVO) {
		this.memberVO = memberVO;
	}
	
	// Oauth2로그인 생성자
	public PrincipalDetails(MemberVO memberVO, Map<String, Object> attributes) {
		this.memberVO = memberVO;
		this.attributes = attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// 해당 사용자의 권한을 리턴하는 곳
		Collection<GrantedAuthority> collect = new ArrayList<>();
		collect.add(() -> memberVO.getRole());
		
		return collect;
	}

	@Override
	public String getPassword() {
		// 사용자 비밀번호를 리턴
		return memberVO.getPassword();
	}

	@Override
	public String getUsername() {
		// 사용자 아이디 리턴
		return memberVO.getMemberId();
	}

	@Override
	public boolean isAccountNonExpired() {
		// 계정 만료되지 않았으면 true
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// 겨정이 잠기지 않았으면 true
		if(memberVO.getLoginFailCnt() < 5) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// 비밀번호의 유효기간이 지나지 않았으면 true
		return true;
	}

	@Override
	public boolean isEnabled() {
		// 계정이 사용가능하면 true
		// 회원이 1년간 로그인하지 않아 휴먼계정 처리할 경우 사용가능.
		if(memberVO.getStopDate() != null) {
			throw new UsernameNotFoundException("탈퇴한 회원");
		} else {
			return true;
		}
	}

	@Override
	public Map<String, Object> getAttributes() {
		// oauth2
		return attributes;
	}

	@Override
	public String getName() {
		// oauth2
		return memberVO.getNickname();
	}

}
