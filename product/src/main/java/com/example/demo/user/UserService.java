package com.example.demo.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.kakao.KakaoService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
// 메서드나 클래스에 적용가능.
// Transactional
// 어노테이션이 적용된 메서드가 호출되면, 새로운 트랜잭션이 시작됨.
// 메서드 실행이 성공적으로 완료되면, 트랜잭션은 자동으로 커밋.
// 메서드 실행 중에 예외가 발생하면, 트랜잭션은 자동으로 롤백.
//
// readOnly = true : 이 설정은 해당 트랜잭션이 데이터를 변경하지 않고 읽기전용으로만 사용이 가능하다는것을 명시적으로 나타냄.
@RequiredArgsConstructor
@Service
public class UserService {

    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public void join(UserRequest.JoinDto joinDto) {
        // 이미 있는 이메일인지 확인
        checkEmail(joinDto.getEmail());

        // 입력한 비밀번호를 인코딩하여 넣음
        String encodedPassword = passwordEncoder.encode(joinDto.getPassword());
        joinDto.setPassword(encodedPassword);

        try {
            userRepository.save(joinDto.toEntity());

            // 자기 전화번호로 회원가입 메세지가 오도록 함 (돈 내야 해서 지금은 안 씀)
            // SignUpMessageSender.sendMessage("01074517172", joinDto.getPhoneNumber(),"환영합니다. 회원가입이 완료되었습니다.");
        } catch (Exception e) {
            throw new Exception500(e.getMessage());
        }
    }

    // id, 비밀번호 인증 후 access_token, refresh_token 생성
    @Transactional
    public UserResponse.UserDto login(UserRequest.JoinDto joinDto, HttpSession session) {
        // 인증 작업
        try{
            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(
                    joinDto.getEmail(), joinDto.getPassword());
            // anonymousUser = 비인증
            Authentication authentication
                    = authenticationManager.authenticate(token);
            // 인증 완료 값을 받아온다.
            // 인증키
            CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

            // 토큰 생성 및 저장
            User user = customUserDetails.getUser();
            String access_token = JwtTokenProvider.create(user);
            String refreshToken = JwtTokenProvider.createRefresh(user);
            user.setToken(access_token, refreshToken);
            session.setAttribute("token", access_token);

            User responseUser = userRepository.save(user);
            return UserResponse.UserDto.toUserDto(responseUser);
        }catch (Exception e){
            throw new Exception401("인증되지 않음.");
        }
    }

    // 이미 존재하는 이메일인지 확인
    public void checkEmail(String email){
        Optional<User> users = userRepository.findByEmail(email);
        if (users.isPresent()){
            throw new Exception400("이미 존재하는 이메일입니다. : " + email);
        }
    }

    // 로그아웃
    @Transactional
    public String logout(Long id, HttpSession session) {
        try {
            User user = userRepository.findById(id).orElseThrow();
            // 카카오톡으로 로그인했을 경우
            if (user.getPlatform().equals("kakao")) {
                //kakaoService.kakaoLogout(user);

            // 일반 회원으로 로그인했을 경우
            } else {
                killToken(user);
                session.invalidate();
            }
            // 메인 화면으로
            return "/";
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    // 회원의 access_token 과 refresh_token 무효화
    public void killToken(User user){
        user.setToken(null, null);
        userRepository.save(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtTokenProvider.invalidateToken(authentication);
    }

    public UserResponse.UserDto getCurrentUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.output();
        return UserResponse.UserDto.toUserDto(user);
    }

    // 회원의 access_token 과 refresh_token 갱신
    @Transactional
    public void refresh(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        String refresh_token = user.getRefresh_token();
        DecodedJWT decodedJWT = JwtTokenProvider.verify(refresh_token);

        // access_token 재발급
        String username = decodedJWT.getSubject();
        User refreshUser = userRepository.findByEmail(username).orElseThrow();
        if (!refreshUser.getRefresh_token().equals(refresh_token))
            throw new Exception401("유효하지 않은 Refresh Token 입니다.");
        String new_access_Token = JwtTokenProvider.create(refreshUser);

        // 현재시간과 refresh_token 만료날짜를 통해 남은 만료기간 계산
        // refresh_token 만료기간을 계산해 5일 미만일 시 refresh_token도 발급
        long endTime = decodedJWT.getClaim("exp").asLong() * 1000;
        long diffDay = (endTime - System.currentTimeMillis()) / 1000 / 60 / 60 / 24;
        if (diffDay < 5) {
            String new_refresh_token = JwtTokenProvider.createRefresh(user);
            refreshUser.setToken(new_access_Token, new_refresh_token);
        } else {
            refreshUser.setToken(new_access_Token, refresh_token);
        }
    }

    // 가입한 모든 회원들 출력
    public void findAll() {
        List<User> all = userRepository.findAll();

        for (User user : all){
            user.output();
            System.out.println();
        }
    }

    // POST 요청 처리
    public <T> ResponseEntity<JsonNode> userPost(String requestUrl, HttpHeaders headers, T body){
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, JsonNode.class);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    // GET 요청 처리
    public <T> ResponseEntity<JsonNode> userGet(String requestUrl, HttpHeaders headers, T body){
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, JsonNode.class);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }


}
