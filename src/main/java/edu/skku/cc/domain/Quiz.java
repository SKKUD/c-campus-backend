package edu.skku.cc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "quiz")
    private Message message;

    @Column(length = 20)
    private String content;

    @Column(length = 7)
    private String answer;

    private Boolean isSolved;

    public void solve() {
        this.isSolved = true;
    }
}
