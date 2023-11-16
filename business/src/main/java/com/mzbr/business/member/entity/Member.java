package com.mzbr.business.member.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.mzbr.business.oauth2.kakao.dto.UserOauthInfo;
import com.mzbr.business.oauth2.userinfo.OAuth2UserInfo;
import com.mzbr.business.store.entity.Store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "memberId")
	private long id;

	private String email;

	private String nickname;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Enumerated(EnumType.STRING)
	private SocialType socialType;

	private String socialId;

	private String profileImage;

	@OneToMany(mappedBy = "members")
	List<Store> stores;

	public void authorizeUser() {
		this.role = Role.MEMBER;
	}

	public static Member of(SocialType socialType, OAuth2UserInfo oAuth2UserInfo) {
		return Member.builder()
			.socialType(socialType)
			.socialId(oAuth2UserInfo.getId())
			.nickname(oAuth2UserInfo.getNickname())
			.role(Role.GUEST)
			.build();
	}

	public static Member of(SocialType socialType, UserOauthInfo userOauthInfo) {
		return Member.builder()
			.socialType(socialType)
			.socialId(userOauthInfo.getSocial_id())
			.nickname(userOauthInfo.getNickName())
			.role(Role.GUEST)
			.build();
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void join(String nickname) {
		this.nickname = nickname;
		this.role = Role.MEMBER;
	}

	public void changeProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

}
