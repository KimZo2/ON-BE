package com.KimZo2.Back.global.util;

import java.util.UUID;

public final class KeyFactory {

    public static String roomMeta(UUID r)          { return "rooms:" + r; }
    public static String roomMembers(UUID r)       { return "rooms:" + r + ":members"; }
    public static String roomPos(UUID r)           { return "rooms:" + r + ":pos"; }
    public static String roomSeen(UUID r)          { return "rooms:" + r + ":seen"; }
    public static String roomNicknames(UUID r)   { return "rooms:" + r + ":nicknames"; }
    public static String userRoom(UUID userId)   { return "users:" + userId + ":rooms"; }
    public static String presence(UUID r, UUID u, String s) { return "presence:" + r + ":" + u + ":" + s; }
//    public static String moveRate(UUID r, UUID u) { return "rate:move:" + r + ":" + u; }
    public static String roomHot()  {return "rooms:hot";}
    public static String roomName(String name) {return "roomsName:" + name;}
    public static String roomPublic() {return "rooms:public";}
    public static String roomActive() {return "rooms:active_list";}
    public static String roomNotify(UUID r) {return "rooms:notify:" + r;}


    public static String ROOM_META_PREFIX = "rooms:";
    public static final String ROOM_NOTIFY_PREFIX = "rooms:notify:";

    public static final String FIELD_VISIBILITY = "visibility";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_TRUE = "true";
    public static final String VALUE_PUBLIC = "0";
}


