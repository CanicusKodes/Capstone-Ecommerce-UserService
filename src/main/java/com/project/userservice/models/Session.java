package com.project.userservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Session extends BaseModel {
    @Column(length = 500)
    private String token;
    private Date expiringAt;
    @ManyToOne
    private User user;
    @Enumerated(EnumType.ORDINAL)
    private SessionStatus sessionStatus;
}
