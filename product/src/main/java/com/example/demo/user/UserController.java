package com.example.demo.user;

import com.example.demo.user.UserRequest;
import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.core.utils.ApiUtils;
import com.example.demo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<Object> join(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.join(joinDto);

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    // 이미 가입한 이메일인지 확인
    @PostMapping("/check")
    public ResponseEntity<Object> check(@RequestBody @Valid UserRequest.JoinDto joinDto, Error error){
        userService.checkEmail(joinDto.getEmail());

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid UserRequest.JoinDto joinDto, HttpServletRequest req, HttpServletResponse res, Error error){
        String jwt = userService.login(joinDto, req.getSession(), res);
        return ResponseEntity.ok().header(JwtTokenProvider.HEADER, jwt)
                .body(ApiUtils.success(null));
    }

    // 로그아웃
    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletResponse res, Error error){
        return userService.logout(customUserDetails.getUser().getId(), res);
    }

    // 가입한 회원들 출력
    @GetMapping("/users")
    public ResponseEntity<Object> printUsers(){
        userService.findAll();

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    // 현재 로그인한 회원의 id (헤더에 토큰 넣어야 함)
    @PostMapping("/user_info")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        if (customUserDetails.getUser() == null){
            return ResponseEntity.ok(ApiUtils.error("현재 로그인된 user가 없습니다.", HttpStatus.UNAUTHORIZED));
        }
        UserResponse.UserDto userDto = userService.getCurrentUser(customUserDetails.getUser().getId());

        return ResponseEntity.ok(ApiUtils.success(userDto));
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<Object> tokenRefresh(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletResponse res) {
        if (customUserDetails.getUser() == null) {
            return ResponseEntity.ok(ApiUtils.error("현재 로그인된 user가 없습니다.", HttpStatus.UNAUTHORIZED));
        }
        userService.refresh(customUserDetails.getUser().getId(), res);
        return ResponseEntity.ok(ApiUtils.success(null));
    }
}
