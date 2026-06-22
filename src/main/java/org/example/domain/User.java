package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.example.domain.enums.Role;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @BsonId
    private String id;  // _id в MongoDB (String)

    @BsonProperty("userId")
    private UUID userId;  // отдельное UUID

    @BsonProperty("username")
    private String username;

    @BsonProperty("email")
    private String email;

    @BsonProperty("passwordHash")
    private String passwordHash;

    @BsonProperty("role")
    private Role role;

    @BsonProperty("isDeleted")
    private boolean isDeleted;

    @BsonProperty("createdAt")
    private LocalDateTime createdAt;

    public User(String username, String email, String passwordHash, Role role) {
        this.userId = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
    }
}