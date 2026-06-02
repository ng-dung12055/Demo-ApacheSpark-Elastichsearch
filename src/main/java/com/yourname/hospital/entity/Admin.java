package com.yourname.hospital.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quan_tri_vien")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String adminCode;

    @OneToOne
    @JoinColumn(name = "nguoi_dung_id")
    private User user;

    public Admin() {
    }

    public Admin(String adminCode, User user) {
        this.adminCode = adminCode;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
