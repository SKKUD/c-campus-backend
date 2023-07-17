package edu.skku.cc.domain;

import jakarta.persistence.*;

@Entity
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "quiz")
    private Message message;

    @Column(length = 20)
    private String content;

    @Column(length=7)
    private String answer;

    private Boolean isSolved;
}
