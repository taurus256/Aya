package org.taurus.aya.server.controllers;

import java.util.List;

public class GenericUserData {
    private Long userId;
    private List<Long> groups;

    public GenericUserData(Long userId, List<Long> groups) {
        this.userId = userId;
        this.groups = groups;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getGroups() {
        return groups;
    }

    public void setGroups(List<Long> groups) {
        this.groups = groups;
    }
}
