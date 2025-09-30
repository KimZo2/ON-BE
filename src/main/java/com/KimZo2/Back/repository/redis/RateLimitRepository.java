package com.KimZo2.Back.repository.redis;

public interface RateLimitRepository {
    /** 증가 후 1이면 TTL 설정. 결과 카운트 반환 */
    long incrWithWindow(String key, int windowSec);
}
