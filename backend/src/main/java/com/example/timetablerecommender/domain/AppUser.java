package com.example.timetablerecommender.domain;
//Spring Boot에서는 보통 @SpringBootApplication이 선언된 메인 클래스의 패키지와
//그 하위 패키지를 스캔함. @Entity가 붙은 클래스가 스캔 범위에 있으면 엔티티로 등록됨.
//@SpringBootApplication이 com.example.timetablerecommender 패키지에 선언되어 있기 때문에
//그 하위 패키지에 속하는 이 파일을 스캔함
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
//entity: DB 테이블 데이터를 Java 프로그램에서 어떤 객체 형태로 사용할지 정의한 클래스
// 엔티티 파일(.java)는 Hibernate가 알아서 만들어주지 않음
// 개발자가 DB 설계를 보고 직접 작성해야함
@Entity//이 클래스가 일반 Java 클래스가 아니라 JPA/Hibernate가 관리한 엔티티 클래스라고
//표시해줌 이 표시가 없으면 Hibernate는 Course를 DB와 연결된 엔티티로 취급안함.
@Table(//이 엔티티를 DB의 app_user과 연결
        name = "app_user",
        uniqueConstraints = @UniqueConstraint(//unique 제약 조건
                name = "uk_app_user_login_id",
                columnNames = "login_id"
        )
)
// new Course(...)
//         ↓
// 단순 Java 객체
//         ↓
// repository.save(course)
// 또는 entityManager.persist(course)
//         ↓
// Hibernate 관리 대상
//         ↓
// flush/commit
//         ↓
// INSERT SQL 실행
//         ↓
// DB 저장
//-------------------------
//단순히 java객체를 만든다고 바로 db에 저장되지 않음
//save나 persist같은 명령어를 추가로 사용해야함.
//그대신 복잡한 SQL명령어를 사용하는 대신 save 객체, persist객체로 쉽게 db에 데이터를
//저장할 수 있음
public class AppUser {

    @Id// 이 필드가 엔티티의 식별자, 즉 DB의 기본키와 연결된다는 뜻
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //GeneratedValue: id 값을 Java 코드에서 직접 지정하지 않고 DB가 생성하도록 설정
    private Long id;

    //이 Java 필드를 어느 DB column과 연결할지 표현함
    @Column(name = "login_id", nullable = false, length = 100)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
    //생성자가 protected, public 2개임

    protected AppUser() {
    }
    //Hibernate가 DB조회 결과로 엔티티 객체를 만들 때 사용할 수 있도록 둔 생성자
    public AppUser(String loginId, String passwordHash, String name) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
    }
    // 개발자가 사용하는 생성자
    // 외부 코드가 빈 엔티티를 함부로 만들지 못하도록 protected를 사용함
    // 왜 Hibernate는 빈 생성자가 필요한가???
    // Hibernate가 DB 조회 결과를 바탕으로 AppUser 객체를 생성할 때 사용하기 위한 생성자임
    // Hibernate는 일단 객체의 틀로 빈 객체를 먼저 만들고 DB에서 읽은 값을 필드에 주입함

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
