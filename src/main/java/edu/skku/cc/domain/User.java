package edu.skku.cc.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name="uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    @Column(nullable = false)
    private String profileImageUuid;
    @Column(nullable = false)
    private String profileImageUrl;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Message> messages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Photo> photos;

    public User(String name, Long kakaoId, String profileImageUrl) {
        this.name = name;
        this.kakaoId = kakaoId;
        this.profileImageUrl = profileImageUrl;
    }

    public void addPhoto(Photo photo) {
        photo.setUser(this);
        photos.add(photo);
    }

    public void saveProfileImageUuid(String profileImageUuid) {
        this.profileImageUuid = profileImageUuid;
    }

    public void saveProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Integer getMessageCount() {
        return messages.size();
    }
}


