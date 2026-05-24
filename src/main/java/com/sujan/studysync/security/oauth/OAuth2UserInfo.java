package com.sujan.studysync.security.oauth;

import java.util.Map;

public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getName();

    String getImageUrl();

    String getProvider();

    Map<String, Object> getAttributes();
}