package ru.practicum.evmmainservice.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.evmmainservice.entity.User;

import java.util.List;

public interface EvmUserRepository extends JpaRepository<User, Long> {

    @Query("select u from User as u " +
            "where u.id = ?1")
    List<User> findAllByIds(List<Long> ids, PageRequest pageRequest);

}
