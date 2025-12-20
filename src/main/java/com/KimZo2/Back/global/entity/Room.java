package com.KimZo2.Back.global.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(name = "password_hash", length = 80)
    private String password;

    @Column(nullable = false)
    private int roomTime;

    private int currentParticipants;

    @Column(nullable = false)
    private long roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(nullable = false)
    private boolean status = true;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime expiresAt;

    // 메서드
    public void makePrivate(String encodedPassword) {
        this.isPrivate = true;
        this.password = encodedPassword;
    }

    // 연관 관계 메서드
    public void setUser(User user) {
        this.creator = user;
        if(!creator.getRooms().contains(this)){
            creator.getRooms().add(this);
        }
    }
}
