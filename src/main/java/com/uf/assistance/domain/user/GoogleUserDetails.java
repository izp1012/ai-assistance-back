//package com.uf.assistance.domain.user;
//
//import lombok.AllArgsConstructor;
//
//import java.util.Map;
//
//@AllArgsConstructor
//public class GoogleUserDetails implements OAuth2UserInfo {
//
//    private Map<String, Object> attributes;
//
//    @Override
//    public String getProvider() {
//        return "google";
//    }
//
//    @Override
//    public String getProviderId() {
//        return (String) ((Map) attributes.get("response")).get("id");
//    }
//
//    @Override
//    public String getEmail() {
//        return (String) ((Map) attributes.get("response")).get("email");
//    }
//
//    @Override
//    public String getName() {
//        return (String) ((Map) attributes.get("response")).get("name");
//    }
//}