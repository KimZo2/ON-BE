package com.KimZo2.Back.util;

import java.util.UUID;

public final class KeyFactory {
    private KeyFactory() {}

    public static String roomMeta(UUID r)          { return "rooms:" + r; }
    public static String roomMembers(UUID r)       { return "rooms:" + r + ":members"; }
    public static String roomPos(UUID r)           { return "rooms:" + r + ":pos"; }
    public static String roomSeen(UUID r)          { return "rooms:" + r + ":seen"; }
//    public static String roomPosVersion(UUID r)    { return "room:" + r + ":pos:ver"; }

    public static String userRoom(UUID userId)   { return "users:" + userId + ":rooms"; }

    public static String presence(UUID r, UUID u, String s) { return "presence:" + r + ":" + u + ":" + s; }
    public static String moveRate(UUID r, UUID u) { return "rate:move:" + r + ":" + u; }
    public static String roomHot()  {return "rooms:hot";}
}
