package com.example.timetablerecommender.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import com.example.timetablerecommender.auth.security.SessionUser;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.example.timetablerecommender.domain.AppUser;
import com.example.timetablerecommender.domain.CompletedCourse;
import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.domain.CourseInterestArea;
import com.example.timetablerecommender.domain.CoursePrerequisite;
import com.example.timetablerecommender.domain.CourseSection;
import com.example.timetablerecommender.domain.InterestArea;
import com.example.timetablerecommender.domain.RelationType;
import com.example.timetablerecommender.domain.SectionTime;
import com.example.timetablerecommender.repository.AppUserRepository;
import com.example.timetablerecommender.repository.CompletedCourseRepository;
import com.example.timetablerecommender.repository.CourseInterestAreaRepository;
import com.example.timetablerecommender.repository.CoursePrerequisiteRepository;
import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;
import com.example.timetablerecommender.repository.InterestAreaRepository;
import com.example.timetablerecommender.repository.SectionTimeRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:api-test;MODE=PostgreSQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.csv-import.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CompletedCourseRepository completedCourseRepository;
    @Autowired
    private CourseInterestAreaRepository courseInterestAreaRepository;
    @Autowired
    private CoursePrerequisiteRepository prerequisiteRepository;
    @Autowired
    private InterestAreaRepository interestAreaRepository;
    @Autowired
    private CourseSectionRepository sectionRepository;
    @Autowired
    private SectionTimeRepository sectionTimeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanData() {
        sectionTimeRepository.deleteAll();
        sectionRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        courseInterestAreaRepository.deleteAll();
        completedCourseRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void healthUsesCommonResponseShape() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ok"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void signupHashesPasswordAndLoginReturnsNoPassword() throws Exception {
        var signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"User@Example.com","password":"password123!","name":"홍길동"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        AppUser saved = userRepository.findByLoginId("user@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", saved.getPasswordHash())).isTrue();
        MockHttpSession signupSession = (MockHttpSession) signupResult.getRequest().getSession(false);
        assertThat(signupSession).isNotNull();
        mockMvc.perform(get("/api/me").session(signupSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(saved.getId()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(saved.getId()))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void signupValidationDuplicateAndLoginFailuresUseCommonErrors() throws Exception {
        userRepository.save(new AppUser(
                "user@example.com", passwordEncoder.encode("password123!"), "기존 사용자"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-email","password":"short","name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fieldErrors.email").exists())
                .andExpect(jsonPath("$.error.fieldErrors.password").exists());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password123!","name":"중복"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_USER"));

        assertInvalidCredentials("missing@example.com", "password123!");
        assertInvalidCredentials("user@example.com", "wrong-password");
    }

    @Test
    void completedCoursesAreSortedAndPutDeleteAreIdempotent() throws Exception {
        AppUser user = userRepository.save(new AppUser("user@example.com", "hash", "사용자"));
        MockHttpSession session = authenticatedSession(user);
        Course later = courseRepository.save(new Course("CS200", "나중 과목", 3, "MAJOR_ELECTIVE"));
        Course earlier = courseRepository.save(new Course("CS100", "먼저 과목", 3, "MAJOR_REQUIRED"));

        mockMvc.perform(put("/api/completed-courses/{courseCode}", later.getCourseCode()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseCode").value("CS200"))
                .andExpect(jsonPath("$.data.completed").value(true));
        mockMvc.perform(put("/api/completed-courses/{courseCode}", later.getCourseCode()).session(session))
                .andExpect(status().isOk());
        completedCourseRepository.save(new CompletedCourse(user, earlier));
        assertThat(completedCourseRepository.count()).isEqualTo(2);

        mockMvc.perform(get("/api/completed-courses").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses[0].courseCode").value("CS100"))
                .andExpect(jsonPath("$.data.courses[1].courseCode").value("CS200"));

        mockMvc.perform(delete("/api/completed-courses/{courseCode}", later.getCourseCode()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(false));
        mockMvc.perform(delete("/api/completed-courses/{courseCode}", later.getCourseCode()).session(session))
                .andExpect(status().isOk());
        assertThat(completedCourseRepository.existsByUserIdAndCourseId(user.getId(), later.getId())).isFalse();
    }

    @Test
    void coursesAreReturnedAsSortedDtosAndMayBeEmpty() throws Exception {
        AppUser user = userRepository.save(new AppUser("user@example.com", "hash", "사용자"));
        MockHttpSession session = authenticatedSession(user);
        mockMvc.perform(get("/api/courses").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.courses").isEmpty())
                .andExpect(jsonPath("$.error").doesNotExist());

        courseRepository.save(new Course("CS300", "알고리즘 개론", 3, "MAJOR_REQUIRED"));
        courseRepository.save(new Course("CS101", "프로그래밍 기초", 2, "BASIC_REQUIRED"));

        mockMvc.perform(get("/api/courses").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses.length()").value(2))
                .andExpect(jsonPath("$.data.courses[0].courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.courses[0].name").value("프로그래밍 기초"))
                .andExpect(jsonPath("$.data.courses[0].credits").value(2))
                .andExpect(jsonPath("$.data.courses[0].id").doesNotExist())
                .andExpect(jsonPath("$.data.courses[0].courseType").doesNotExist())
                .andExpect(jsonPath("$.data.courses[1].courseCode").value("CS300"));
    }

    @Test
    void completedCourseEndpointsReturnNotFoundAndEmptyList() throws Exception {
        AppUser user = userRepository.save(new AppUser("user@example.com", "hash", "사용자"));
        MockHttpSession session = authenticatedSession(user);
        Course course = courseRepository.save(new Course("CS100", "과목", 3, "MAJOR_REQUIRED"));

        mockMvc.perform(get("/api/completed-courses").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses").isEmpty());
        mockMvc.perform(delete("/api/completed-courses/{courseCode}", "UNKNOWN").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("COURSE_NOT_FOUND"));
    }

    @Test
    void recommendationAppliesCompletionAreasPrerequisitesScoresAndLimit() throws Exception {
        AppUser user = userRepository.save(new AppUser("user@example.com", "hash", "사용자"));
        MockHttpSession session = authenticatedSession(user);
        InterestArea theory = interestAreaRepository.findByName("THEORY").orElseThrow();
        InterestArea security = interestAreaRepository.findByName("SECURE_COMPUTING").orElseThrow();
        Course completed = courseRepository.save(new Course("CS100", "이수", 3, "MAJOR_REQUIRED"));
        Course interested = courseRepository.save(new Course("CS200", "관심 일치", 3, "MAJOR_ELECTIVE"));
        Course required = courseRepository.save(new Course("CS300", "전공필수", 3, "MAJOR_REQUIRED"));
        Course excluded = courseRepository.save(new Course("CS400", "제외", 3, "MAJOR_REQUIRED"));
        Course unmet = courseRepository.save(new Course("CS500", "선수 미충족", 3, "MAJOR_REQUIRED"));
        Course missingPrerequisite = courseRepository.save(new Course("CS050", "미이수 선수", 3, "MAJOR_ELECTIVE"));
        completedCourseRepository.save(new CompletedCourse(user, completed));
        courseInterestAreaRepository.save(new CourseInterestArea(interested, theory));
        courseInterestAreaRepository.save(new CourseInterestArea(excluded, security));
        prerequisiteRepository.save(new CoursePrerequisite(required, completed, RelationType.PREREQUISITE));
        prerequisiteRepository.save(new CoursePrerequisite(unmet, missingPrerequisite, RelationType.PREREQUISITE));
        addSection(interested, "A", DayOfWeek.MONDAY, "09:00", "10:00");
        addSection(required, "A", DayOfWeek.TUESDAY, "09:00", "10:00");
        addSection(excluded, "A", DayOfWeek.WEDNESDAY, "09:00", "10:00");
        addSection(unmet, "A", DayOfWeek.THURSDAY, "09:00", "10:00");

        mockMvc.perform(get("/api/recommended-timetables").session(session)
                        .param("targetCourseCount", "2")
                        .param("studentYear", "THIRD_YEAR")
                        .param("interestedAreaIds", theory.getId().toString())
                        .param("uninterestedAreaIds", security.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetCourseCount").value(2))
                .andExpect(jsonPath("$.data.studentYear").value("THIRD_YEAR"))
                .andExpect(jsonPath("$.data.timetable.score").value(95))
                .andExpect(jsonPath("$.data.timetable.courseCount").value(2))
                .andExpect(jsonPath("$.data.timetable.courses.length()").value(2))
                .andExpect(jsonPath("$.data.timetable.courses[0].courseCode").value("CS200"))
                .andExpect(jsonPath("$.data.timetable.courses[1].courseCode").value("CS300"));
    }

    @Test
    void recommendationValidatesCountAreasAndUser() throws Exception {
        AppUser user = userRepository.save(new AppUser("user@example.com", "hash", "사용자"));
        MockHttpSession session = authenticatedSession(user);
        InterestArea theory = interestAreaRepository.findByName("THEORY").orElseThrow();

        mockMvc.perform(get("/api/recommended-timetables").session(session)
                        .param("targetCourseCount", "0")
                        .param("studentYear", "FIRST_YEAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fieldErrors.targetCourseCount").exists());
        mockMvc.perform(get("/api/recommended-timetables").session(session)
                        .param("targetCourseCount", "21")
                        .param("studentYear", "FOURTH_YEAR_OR_ABOVE"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/recommended-timetables").session(session)
                        .param("targetCourseCount", "1")
                        .param("studentYear", "SECOND_YEAR")
                        .param("interestedAreaIds", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("INTEREST_AREA_NOT_FOUND"));
        mockMvc.perform(get("/api/recommended-timetables").session(session)
                        .param("targetCourseCount", "1")
                        .param("studentYear", "FIFTH_YEAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void protectedApisRequireSessionAndLogoutInvalidatesIt() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/completed-courses"))
                .andExpect(status().isUnauthorized());

        AppUser user = userRepository.save(new AppUser(
                "user@example.com", passwordEncoder.encode("password123!"), "사용자"));
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(user.getId()));
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    private void assertInvalidCredentials(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    private void addSection(
            Course course,
            String sectionNumber,
            DayOfWeek day,
            String start,
            String end) {
        CourseSection section = sectionRepository.save(
                new CourseSection(course, 2026, "FALL", sectionNumber));
        sectionTimeRepository.save(new SectionTime(
                section, day, LocalTime.parse(start), LocalTime.parse(end)));
    }

    private MockHttpSession authenticatedSession(AppUser user) {
        var principal = new SessionUser(user.getId(), user.getLoginId(), user.getName());
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, java.util.List.of());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return session;
    }
}
