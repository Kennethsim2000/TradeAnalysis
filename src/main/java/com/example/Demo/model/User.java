package com.example.Demo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @OneToOne
    @JoinColumn(name = "partner_id")
    private User partner;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;
}
/*create table
  public.users (
    id bigserial not null,
    password character varying(255) null,
    username character varying(255) null,
    partner_id bigint null,
    date_created timestamp without time zone not null,
    constraint users_pkey primary key (id),
    constraint uk_sr3rf8kjb54t8ryfh9tk2156k unique (partner_id),
    constraint fkeenhxwkw7u34s1uivix91ipb8 foreign key (partner_id) references users (id)
  ) tablespace pg_default;
   */
