package com.KimZo2.Back.repository.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RoomListRepository {
    long countPublic();

    List<String> findPublicIdsDesc(long offset, int limit);

    List<Map<String, String>> findRoomsAsHashes(List<String> ids);

    void removeFromPublicIndex(Collection<String> ids);
}
