package edu.skku.cc.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "user")
    private List<Message> messages;

    @OneToMany(mappedBy = "user")
    private List<Photo> photos;

    public Integer getMessageCount() {
        return messages.size();
    }

    public User (String name, String email) {
        this.name = name;
        this.email = email;
    }
}


