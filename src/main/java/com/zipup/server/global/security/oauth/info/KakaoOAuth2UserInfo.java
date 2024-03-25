package com.zipup.server.global.security.oauth.info;

import java.util.Map;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

	public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	@SuppressWarnings("unchecked")
	private <T> Map<String, T> getProperties(Map<String, Object> attributes) {
		return (Map<String, T>) attributes.get("properties");
	}

	@Override
	public String getId() {
		return attributes.get("id").toString();
	}

	@Override
	public String getName() {
		Map<String, Object> properties = getProperties(attributes);

		if (properties == null) return null;

		return (String) properties.get("nickname");
	}

	@Override
	public String getImageUrl() {
		Map<String, Object> properties = getProperties(attributes);

		if (properties == null) return null;

		return (String) properties.get("profile_image");
	}

	@SuppressWarnings("unchecked")
	private <T> Map<String, T> getAccounts(Map<String, Object> attributes) {
		return (Map<String, T>) attributes.get("kakao_account");
	}

	@Override
	public String getEmail() {
		Map<String, Object> account = getAccounts(attributes);

		return (String) account.get("email");
	}

}
