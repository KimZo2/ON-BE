package com.KimZo2.Back.domain.room.repository;

import com.KimZo2.Back.global.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findById(UUID roomId);
}
