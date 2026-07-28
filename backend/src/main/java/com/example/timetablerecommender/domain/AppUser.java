package com.example.timetablerecommender.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * app_user 테이블을 Java 객체로 표현한 JPA 엔티티이다.
 *
 * 엔티티 클래스는 Hibernate가 자동으로 작성하지 않으므로 개발자가 DB 설계에 맞춰
 * 작성해야 한다. 이 클래스는 메인 클래스의 하위 패키지에 있어 컴포넌트 스캔 및 엔티티
 * 탐색 범위에 포함된다.
 *
 * 객체를 new AppUser(...)로 생성하는 것만으로 DB에 저장되지는 않는다.
 * Repository의 save() 또는 EntityManager의 persist()로 영속화한 뒤
 * flush/commit 시점이 되어야 INSERT SQL이 실행된다.
 */
@Entity
@Table(
        name = "app_user",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_app_user_login_id",
                columnNames = "login_id"
        )
)
public class AppUser {

    // @Id는 엔티티 식별자이자 DB 기본 키와 연결되는 필드임을 뜻한다.
    @Id
    // IDENTITY 전략을 사용하므로 id를 Java에서 지정하지 않고 DB가 생성한다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column은 Java 필드와 DB 열의 이름 및 제약 조건을 연결한다.
    @Column(name = "login_id", nullable = false, length = 100)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /*
     * JPA/Hibernate가 조회 결과로 엔티티를 만들 때 사용하는 기본 생성자이다.
     * 프레임워크는 먼저 빈 객체를 만든 후 DB에서 읽은 값을 필드에 채운다.
     * protected로 두면 JPA 요구 사항을 만족하면서 외부 코드가 빈 엔티티를 함부로
     * 생성하는 것도 막을 수 있다.
     */
    protected AppUser() {
    }

    // 애플리케이션 코드에서 필수 값을 갖춘 사용자를 만들 때 사용하는 생성자이다.
    public AppUser(String loginId, String passwordHash, String name) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AppUser appUser)) {
            return false;
        }
        return id != null && Objects.equals(id, appUser.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
