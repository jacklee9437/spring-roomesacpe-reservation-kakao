package nextstep.service;

import nextstep.domain.Reservation;
import nextstep.domain.Theme;
import nextstep.dto.ReservationRequestDto;
import nextstep.dto.ReservationResponseDto;
import nextstep.exceptions.exception.DuplicatedDateAndTimeException;
import nextstep.repository.ReservationDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;

    public ReservationService(@Qualifier("reservationJdbcTemplateDao") ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public Long reserve(ReservationRequestDto reservationRequestDto) {
        LocalDate date = reservationRequestDto.getDate();
        LocalTime time = reservationRequestDto.getTime();

        if (reservationDao.countByDateAndTime(date, time) > 0) {
            throw new DuplicatedDateAndTimeException();
        }

        Theme theme = new Theme("워너고홈", "병맛 어드벤처 회사 코믹물", 29_000);
        Reservation newReservation = new Reservation(date, time, reservationRequestDto.getName(), theme);

        return reservationDao.save(newReservation);
    }

    public ReservationResponseDto retrieve(Long id) {
        Reservation reservation = reservationDao.findById(id);
        if (reservation != null) {
            return new ReservationResponseDto(
                    reservation.getId(),
                    reservation.getDate(),
                    reservation.getTime(),
                    reservation.getName(),
                    reservation.getTheme().getName(),
                    reservation.getTheme().getDesc(),
                    reservation.getTheme().getPrice()
            );
        }
        return null;
    }

    public void delete(Long id) {
        reservationDao.delete(id);
    }
}
