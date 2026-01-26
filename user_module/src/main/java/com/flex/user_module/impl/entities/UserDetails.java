package com.flex.user_module.impl.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/25/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_details")
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "userId")
    private User user;
    //encrypt this
    private String nic;
    private String contact;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Colombo")
    @Column(columnDefinition = "TIMESTAMP default CURRENT_TIMESTAMP")
    private Date addedTime;
}
