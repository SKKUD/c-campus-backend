package edu.skku.cc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String category;

    @Column(nullable = false)
    private String content;

    private String author;

    private Boolean isOpened; // 수신자가 열람했는지 여부

    private Boolean isPulled; // 뽑혔는지 여부

    private LocalDateTime pulledAt; // 뽑힌 날짜, 시간

    @OneToOne(mappedBy = "message", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Photo photo;

    private String backgroundColorCode;

    private Boolean isPublic; // 공개 여부

    @OneToOne(mappedBy = "message", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Quiz quiz;

    public UUID getImageUuid() {
        return this.photo != null ? this.photo.getImageUuid() : null;
    }

    public void updateIsPublic() {
        this.isPublic = !this.isPublic;
    }

    public void pullMessage() {
        this.isPulled = true;
        this.pulledAt = LocalDateTime.now();
    }

    public Quiz setQuiz(String content, String answer) {
        this.quiz = Quiz.builder()
                .message(this)
                .content(content)
                .answer(answer)
                .isSolved(false)
                .build();
        return this.quiz;
    }

    public Photo setPhoto(UUID imageUuid) {
        this.photo = Photo.builder()
                .message(this)
                .imageUuid(imageUuid)
                .user(this.user)
                .build();
        return this.photo;
    }

    public void solveQuiz() {
        this.quiz.solve();
    }

    public void openMessage() {
        this.isOpened = true;
    }
}
