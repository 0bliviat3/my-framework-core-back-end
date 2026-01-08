package com.wan.framework.user.web;

import com.wan.framework.user.dto.UserDTO;
import com.wan.framework.user.service.SignService;
import com.wan.framework.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 REST API
 * - 회원가입
 * - 사용자 정보 수정/삭제
 * - 사용자 조회
 *
 * Note: 로그인/로그아웃은 SessionController 사용
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final SignService signService;
    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody UserDTO userDTO) {
        log.info("POST /users/sign-up - userId: {}", userDTO.getUserId());
        signService.signUp(userDTO);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping
    public ResponseEntity<UserDTO> modifyUser(@RequestBody UserDTO userDTO) {
        log.info("PUT /users - userId: {}", userDTO.getUserId());
        UserDTO user = signService.modifyUser(userDTO);
        // removePass() 메서드로 비밀번호 정보 제거
        return ResponseEntity.ok(user.removePass());
    }

    /**
     * 사용자 삭제 (회원 탈퇴)
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestBody UserDTO userDTO) {
        log.info("DELETE /users - userId: {}", userDTO.getUserId());
        signService.deleteUser(userDTO);
        return ResponseEntity.ok("삭제 성공");
    }

    /**
     * 사용자 목록 조회 (페이징)
     */
    @GetMapping
    public Page<UserDTO> getUserList(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        log.info("GET /users - page: {}, size: {}", pageNumber, pageSize);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return signService.findAll(pageRequest);
    }

    /**
     * 특정 사용자 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        log.info("GET /users/{}", userId);
        UserDTO user = signService.findById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 ID 존재 여부 확인
     */
    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> isExistUser(@PathVariable String userId) {
        log.info("GET /users/exists/{}", userId);
        boolean exists = signService.isExistUserId(userId);
        return ResponseEntity.ok(exists);
    }

    /**
     * 관리자 계정 존재 여부 확인
     * - 최초 접속 시 관리자 계정이 있는지 확인하기 위한 API
     * - 세션 없이 접근 가능 (SecurityConfig에서 허용 필요)
     */
    @GetMapping("/admin/exists")
    public ResponseEntity<Boolean> hasAdminAccount() {
        log.info("GET /users/admin/exists");
        boolean hasAdmin = userService.hasAdminAccount();
        return ResponseEntity.ok(hasAdmin);
    }

    /**
     * 초기 관리자 계정 생성
     * - 관리자 계정이 없을 때만 사용 가능
     * - ROLE_ADMIN 권한 자동 부여
     * - 세션 없이 접근 가능
     */
    @PostMapping("/admin/initial")
    public ResponseEntity<String> createInitialAdmin(@RequestBody UserDTO userDTO) {
        log.info("POST /users/admin/initial - userId: {}", userDTO.getUserId());
        signService.createInitialAdmin(userDTO);
        return ResponseEntity.ok("초기 관리자 계정이 생성되었습니다.");
    }

}
