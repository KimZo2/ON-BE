package com.KimZo2.Back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "users")
@ToString
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String birthday;

    @Column(nullable = false)
    private boolean agreement;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Room> rooms = new ArrayList<>();

    // 연관 관계 메서드
    public void addRooms(Room room){
        rooms.add(room);
        room.setUser(this);
    }


}
