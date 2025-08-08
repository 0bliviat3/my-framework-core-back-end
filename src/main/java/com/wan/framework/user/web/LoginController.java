package com.wan.framework.user.web;


import com.wan.framework.user.dto.UserDTO;
import com.wan.framework.user.service.SignService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final SignService signService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody UserDTO userDTO) {
        signService.signUp(userDTO);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserDTO> signIn(@RequestBody UserDTO userDTO, HttpSession session, HttpServletResponse response) {
        UserDTO user = signService.signIn(userDTO);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getName());
        setCookies(user, response);

        user.setPassword(null);
        user.setPasswordSalt(null);
        return ResponseEntity.ok(user);
    }

    private void setCookies(UserDTO user, HttpServletResponse response) {
        Cookie idCookie = new Cookie("userId", user.getUserId());
        Cookie nameCookie = new Cookie("userName", user.getName());
        idCookie.setPath("/");
        nameCookie.setPath("/");
        response.addCookie(idCookie);
        response.addCookie(nameCookie);
    }

    @GetMapping("/sign-out")
    public ResponseEntity<String> signOut(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공");
    }

    @PutMapping("/user")
    public ResponseEntity<UserDTO> modifyUser(@RequestBody UserDTO userDTO) {
        UserDTO user = signService.modifyUser(userDTO);
        user.setPasswordSalt(null);
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestBody UserDTO userDTO, HttpSession session) {
        UserDTO user = signService.deleteUser(userDTO);
        boolean valid = session.getAttribute("userId").equals(user.getUserId());
        if (valid) {
            session.invalidate();
        }
        return ResponseEntity.ok("삭제 성공");
    }

    @GetMapping("/users")
    public Page<UserDTO> getUserList(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return signService.findAll(pageRequest);
    }

    @GetMapping("/user")
    public boolean isExistUser(String userId) {
        return signService.isExistUserId(userId);
    }

}
