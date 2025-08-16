package com.makersworld.oauth_inclass.service;

import com.makersworld.oauth_inclass.dto.UserInfoResponse;

public interface OAuth2ProviderService {
    String getId();
    String getAuthorizationUrl();
    String getAccessToken(String code);
    UserInfoResponse getUserInfo(String accessToken);
}
