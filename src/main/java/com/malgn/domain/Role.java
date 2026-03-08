package com.malgn.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ADMIN,
    USER;

    @JsonCreator
    public static Role from(String value) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 비교
                return role;
            }
        }
        return null;
    }
}
