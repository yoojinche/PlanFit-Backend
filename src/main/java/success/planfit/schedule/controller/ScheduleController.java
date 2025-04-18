package success.planfit.schedule.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import success.planfit.global.controller.ControllerUtil;
import success.planfit.global.controller.PlanfitExceptionHandler;
import success.planfit.schedule.dto.ShareSerialDto;
import success.planfit.schedule.dto.request.ScheduleCurrentSequenceUpdateRequestDto;
import success.planfit.schedule.dto.request.ScheduleRequestDto;
import success.planfit.schedule.dto.response.ScheduleResponseDto;
import success.planfit.schedule.dto.response.ScheduleTitleInfoResponseDto;
import success.planfit.schedule.service.ScheduleService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("schedule")
@AllArgsConstructor
public class ScheduleController {

    private final ControllerUtil util;
    private final PlanfitExceptionHandler exceptionHandler;
    private final ScheduleService scheduleService;

    /**
     * 일정 등록
     */
    @PostMapping
    public ResponseEntity<Void> registerSchedule(Principal principal, ScheduleRequestDto requestDto) {
        log.info("ScheduleController.registerSchedule() called");

        long userId = util.findUserIdByPrincipal(principal);
        scheduleService.registerSchedule(userId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 일정 삭제
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(Principal principal, @PathVariable long scheduleId) {
        log.info("ScheduleController.deleteSchedule() called");

        long userId = util.findUserIdByPrincipal(principal);
        scheduleService.deleteSchedule(userId, scheduleId);

        return ResponseEntity.ok().build();
    }

    /**
     * 지난 일정 조회
     */
    @GetMapping("/past")
    public ResponseEntity<List<ScheduleTitleInfoResponseDto>> findPastSchedules(Principal principal) {
        log.info("ScheduleController.findPastSchedules() called");

        long userId = util.findUserIdByPrincipal(principal);
        List<ScheduleTitleInfoResponseDto> responseDtos = scheduleService.findPastSchedules(userId, LocalDate.now());

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * 다가올 일정(아직 지나지 않은 일정) 조회
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleTitleInfoResponseDto>> findUpcomingSchedules(Principal principal) {
        log.info("ScheduleController.findUpcomingSchedules() called");

        long userId = util.findUserIdByPrincipal(principal);
        List<ScheduleTitleInfoResponseDto> responseDtos = scheduleService.findUpcomingSchedules(userId, LocalDate.now());

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * 일정 상세 조회
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> findScheduleDetail(Principal principal, @PathVariable long scheduleId) {
        log.info("ScheduleController.findScheduleDetail() called");

        long userId = util.findUserIdByPrincipal(principal);
        ScheduleResponseDto responseDto = scheduleService.findScheduleDetail(userId, scheduleId);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 일정 수정
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(Principal principal, @PathVariable long scheduleId, ScheduleRequestDto requestDto) {
        log.info("ScheduleController.updateSchedule() called");

        long userId = util.findUserIdByPrincipal(principal);
        scheduleService.update(userId, scheduleId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 일정 장소 방문
     */
    @PatchMapping("/visit")
    public ResponseEntity<Void> visitScheduleSpace(Principal principal, ScheduleCurrentSequenceUpdateRequestDto requestDto) {
        log.info("ScheduleController.visitScheduleSpace() called");

        long userId = util.findUserIdByPrincipal(principal);
        scheduleService.updateCurrentSequence(userId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 일정 공유 링크 생성
     */
    @PostMapping("/share/{scheduleId}")
    public ResponseEntity<ShareSerialDto> createShareSerial(Principal principal, @PathVariable Long scheduleId) {
        log.info("ScheduleController.createShareSerial() called");

        long userId = util.findUserIdByPrincipal(principal);
        ShareSerialDto shareSerialDto = scheduleService.createShareSerial(userId, scheduleId);

        return ResponseEntity.ok(shareSerialDto);
    }

    /**
     * 일정 공유 링크를 통해 일정 조회
     */
    @GetMapping("/share/view/{shareSerial}")
    public ResponseEntity<ScheduleResponseDto> findScheduleByShareSerial(@PathVariable String shareSerial) {
        log.info("ScheduleController.findScheduleByShareSerial() called");

        ScheduleResponseDto responseDto = scheduleService.findByShareSerial(shareSerial);

        return ResponseEntity.ok(responseDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        log.info("ScheduleController.handleException() called");

        return exceptionHandler.handle(exception);
    }

}
