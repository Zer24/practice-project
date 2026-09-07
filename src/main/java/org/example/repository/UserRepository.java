package org.example.repository;

import org.example.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<User> findByIsDeletedFalse(Pageable pageable);

    boolean existsByUsernameAndIsDeletedFalse(String username);
    boolean existsByEmailAndIsDeletedFalse(String email);

    @Query("{ 'isDeleted': false }")
    List<User> findAllActive();
    @Query("{ 'isDeleted': false, $and: [ " +
            "{ $or: [ { 'role': ?0 }, { 'role': { $exists: true } } ] }, " +
            "{ $or: [ { 'username': ?1 }, { 'username': { $exists: true } } ] }, " +
            "{ $or: [ { 'email': ?2 }, { 'email': { $exists: true } } ] } " +
            "] }")
    Page<User> findActiveUsersByFilters(String role, String username, String email, Pageable pageable);
}