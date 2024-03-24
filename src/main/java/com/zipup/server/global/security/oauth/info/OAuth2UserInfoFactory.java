package com.zipup.server.global.security.oauth.info;

import com.zipup.server.global.util.entity.LoginProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {
	public static OAuth2UserInfo getOAuth2UserInfo(LoginProvider providerType, Map<String, Object> attributes) {
		if (providerType == LoginProvider.KAKAO) {
			return new KakaoOAuth2UserInfo(attributes);
		} else {
			throw new IllegalArgumentException("Invalid Provider Type.");
		}
	}
}
