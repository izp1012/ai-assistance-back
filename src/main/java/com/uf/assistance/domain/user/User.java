package com.uf.assistance.domain.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_tb")
@Entity
@DynamicUpdate
public class User { //extends 시간설정 (상속)
    @Id
    @Column(updatable = false, unique = true)
    private String userId;

    @Column(nullable = false, length = 60) //패스워드 인코딩(BCrypt)
    private String password;

    @Column(nullable = false, length = 21, unique = true) //id
    private String username;

    private String nickname;

    private LocalDate birth;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role; //ADMIN, CUSTOMER, DEVELOPER

    @Column
    private boolean social;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String zipcode;
    private String street;
    private String addressDetail;
    private String phoneNo;

    @CreatedDate //Insert
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate //Insert, Update
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String userId, String username, String password, String email, UserRole role, boolean social, Provider provider, LocalDateTime createdAt){
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.social = social;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    public User updateUser(String username, String email) {
        this.username = username;
        this.email = email;

        return this;
    }

//    public void updateRole(UserRole role) {
//        if(this.roles == null) {
//            this.roles = new ArrayList<>();
//        }
//
//        if(this.roles.contains(role)) {
//            this.roles.remove(role);
//        }
//        else {
//            this.roles.add(role);
//        }
//    }
    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateSocial(Provider provider) {
        this.social = true;
        this.provider = provider;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}