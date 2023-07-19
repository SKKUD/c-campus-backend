package edu.skku.cc.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

    private Boolean isOpened; // 수신자가 열람했는지 여부

    private Boolean isPulled; // 뽑혔는지 여부

    private LocalDateTime pulledAt; // 뽑힌 날짜, 시간

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    private String backgroundColorCode;

    private Boolean isPublic; // 공개 여부

    @OneToOne @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}
