package org.example.repository;

import org.example.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);

    boolean existsByUsernameAndIsDeletedFalse(String username);
    boolean existsByEmailAndIsDeletedFalse(String email);

    @Query("{ 'isDeleted': false }")
    List<User> findAllActive();
}