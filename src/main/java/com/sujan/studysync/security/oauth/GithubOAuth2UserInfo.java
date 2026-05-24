package com.sujan.studysync.security.oauth;

import java.util.Map;

public class GithubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {

        String email = (String) attributes.get("email");

        if (email == null) {
            String login = (String) attributes.get("login");
            email = login + "@github.local";
        }

        return email;
    }

    @Override
    public String getName() {

        String name = (String) attributes.get("name");

        if (name == null || name.isBlank()) {
            return (String) attributes.get("login");
        }

        return name;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}