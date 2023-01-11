package nextstep.controller;

import io.restassured.RestAssured;
import nextstep.dto.ReservationRequestDto;
import nextstep.repository.ReservationJdbcTemplateDao;
import nextstep.service.ReservationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.core.Is.is;


@DisplayName("Reservation Test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReservationControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    ReservationService reservationService;
    @Autowired
    ReservationJdbcTemplateDao reservationJdbcTemplateDao;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        ReservationRequestDto requestDto = new ReservationRequestDto(
                LocalDate.parse("2023-01-10"),
                LocalTime.parse("13:00"),
                "jay"
        );
        reservationService.reserve(requestDto);
    }

    @AfterEach
    void afterEach() {
        reservationJdbcTemplateDao.clear();
    }

    @DisplayName("중복이 없는 데이터로 예약 요청시 예약이 성공되어야 함")
    @Test
    void reserve() {
        ReservationRequestDto requestDto = new ReservationRequestDto(
                LocalDate.parse("2023-01-11"),
                LocalTime.parse("13:00:00"),
                "jay"
        );

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDto)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", "/reservations/2");
    }

    @DisplayName("중복된 날짜와 시간으로 예약시 예외처리 되어야 함")
    @Test
    void reserveException() {
        ReservationRequestDto requestDto = new ReservationRequestDto(
                LocalDate.parse("2023-01-10"),
                LocalTime.parse("13:00:00"),
                "jay"
        );

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDto)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(is("이미 예약된 날짜와 시간입니다."));
    }

    @DisplayName("등록된 id 로 예약 조회시 조회 되어야 함")
    @Test
    void show() {
        RestAssured.given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(1))
                .body("date", is("2023-01-10"))
                .body("time", is("13:00:00"))
                .body("name", is("jay"))
                .body("themeName", is("워너고홈"))
                .body("themeDesc", is("병맛 어드벤처 회사 코믹물"))
                .body("themePrice", is(29000));
    }

    @DisplayName("예약 취소가 정상적으로 이루어져야 함")
    @Test
    void delete() {
        RestAssured.given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}