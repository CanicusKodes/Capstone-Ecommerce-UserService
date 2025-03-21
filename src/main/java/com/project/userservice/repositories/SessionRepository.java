package com.project.userservice.repositories;

import com.project.userservice.models.Session;
import com.project.userservice.models.SessionStatus;
import com.project.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByTokenAndUser_Id(String token, Long userId);

    List<Session> findAllByUser(User user);

    List<Session> findAllByUser_Id(Long id);

    List<Session> findAllByUser_IdAndSessionStatus(Long id, SessionStatus i);
    //select * from sessions where token = <> and userId = <>
}
