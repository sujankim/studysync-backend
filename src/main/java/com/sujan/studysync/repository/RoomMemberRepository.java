package com.sujan.studysync.repository;

import com.sujan.studysync.model.RoomMember;
import com.sujan.studysync.model.StudyRoom;
import com.sujan.studysync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findByRoomAndUser(StudyRoom room, User user);

    boolean existsByRoomAndUser(StudyRoom room, User user);

    void deleteByRoomAndUser(StudyRoom room, User user);
}
