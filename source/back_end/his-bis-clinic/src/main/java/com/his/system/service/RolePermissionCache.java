package com.his.system.service;

import java.util.List;

public interface RolePermissionCache {

    public static final String KEY_PREFIX = "his:perm:role:";

    List<String> permissionsOfRole(String roleCode);

    void invalidate(String roleCode);

    void invalidateAll();
}
