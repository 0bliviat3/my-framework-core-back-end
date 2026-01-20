package com.wan.framework.history.web;

import com.wan.framework.history.dto.ErrorHistoryDTO;
import com.wan.framework.history.service.ErrorHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/error-histories")
public class ErrorHistoryController {

    private final ErrorHistoryService errorHistoryService;

    /**
     * 에러 로그 단일 조회
     */
    @GetMapping("/{errorId}")
    public ResponseEntity<ErrorHistoryDTO> getErrorHistory(@PathVariable Long errorId) {
        log.debug("에러 로그 조회 요청: {}", errorId);
        ErrorHistoryDTO errorHistory = errorHistoryService.findById(errorId);
        return ResponseEntity.ok(errorHistory);
    }

    /**
     * 에러 로그 전체 목록 조회 (최신순 페이징)
     */
    @GetMapping
    public Page<ErrorHistoryDTO> getErrorHistoryList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        log.debug("에러 로그 목록 조회 요청 - page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTime"));
        return errorHistoryService.findAll(pageRequest);
    }

    /**
     * URL 기준 에러 로그 검색
     */
    @GetMapping("/search/url")
    public Page<ErrorHistoryDTO> searchByUrl(
            @RequestParam String url,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        log.debug("URL 기준 에러 로그 검색 - url: {}, page: {}, size: {}", url, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTime"));
        return errorHistoryService.findByRequestURL(url, pageRequest);
    }

    /**
     * 에러 메시지 기준 검색
     */
    @GetMapping("/search/message")
    public Page<ErrorHistoryDTO> searchByMessage(
            @RequestParam String message,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        log.debug("에러 메시지 기준 검색 - message: {}, page: {}, size: {}", message, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTime"));
        return errorHistoryService.findByErrorMessage(message, pageRequest);
    }

    /**
     * 기간 기준 에러 로그 조회
     */
    @GetMapping("/search/period")
    public Page<ErrorHistoryDTO> searchByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        log.debug("기간 기준 에러 로그 검색 - start: {}, end: {}, page: {}, size: {}",
                startDate, endDate, page, size);

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTime"));

        return errorHistoryService.findByEventTimeBetween(start, end, pageRequest);
    }

    /**
     * 최근 에러 로그 조회 (시간 제한)
     */
    @GetMapping("/recent")
    public Page<ErrorHistoryDTO> getRecentErrors(
            @RequestParam(value = "hours", defaultValue = "24") int hours,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        log.debug("최근 {}시간 에러 로그 조회 - page: {}, size: {}", hours, page, size);
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTime"));

        return errorHistoryService.findByEventTimeAfter(since, pageRequest);
    }
}
