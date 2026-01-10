package fr.euphyllia.skyllia.api.permissions;

public interface PermissionIndexStore {
    int getOrAllocate(String nodeKey);
}
