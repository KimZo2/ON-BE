package com.KimZo2.Back.domain.user.repository;

import java.util.UUID;

public interface MembersRepository {

    boolean isMember(UUID roomId, String userId);

    void addMember(UUID roomId, String userId);

    void removeMember(UUID roomId, String userId);

    long count(UUID roomId);
}
