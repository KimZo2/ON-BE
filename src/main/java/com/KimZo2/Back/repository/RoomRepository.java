package com.KimZo2.Back.repository;

import com.KimZo2.Back.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    boolean existsByNameIgnoreCaseAndStatus(String name, boolean status);

    Optional<Room> findById(UUID roomId);
}
