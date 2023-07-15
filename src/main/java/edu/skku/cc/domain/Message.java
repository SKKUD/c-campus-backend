package edu.skku.cc.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    private String category;

    @Column(nullable = false)
    private String content;

    private String author;

    private LocalDateTime openedAt; // isOpened 대용

    private String imageUrl;

    private String backgroundColorCode;

    private Boolean isPublic;

    @OneToOne @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}
