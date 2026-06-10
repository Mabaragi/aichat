package com.example.aichat.common.security;

public record RequestActor(
        ActorType type,
        Long userId
) {
    public static RequestActor authenticated(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return new RequestActor(ActorType.AUTHENTICATED, userId);
    }

    public static RequestActor anonymous() {
        return new RequestActor(ActorType.ANONYMOUS, null);
    }

    public static RequestActor system() {
        return new RequestActor(ActorType.SYSTEM, null);
    }

    public boolean canManage(Long ownerId) {
        return type == ActorType.SYSTEM
                || (type == ActorType.AUTHENTICATED && userId.equals(ownerId));
    }

    public enum ActorType {
        AUTHENTICATED,
        ANONYMOUS,
        SYSTEM
    }
}
