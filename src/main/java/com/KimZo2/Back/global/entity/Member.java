package com.KimZo2.Back.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "members")
@ToString(exclude = "rooms")
public class Member implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = true)
    private String nickname;

    @Column(nullable = true)
    private String birthday;

    @Column(nullable = true)
    private boolean agreement;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    // 연관 관계 메서드
    public void addRooms(Room room){
        rooms.add(room);
        room.setUser(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return this.id.toString();
    }
}
