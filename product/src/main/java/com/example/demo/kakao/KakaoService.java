package com.example.demo.kakao;

import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.user.StringArrayConverter;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jdk.swing.interop.SwingInterOpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;

@Transactional(readOnly = true) // 데이터 안정성을 위해 넣음
@RequiredArgsConstructor // 생성자
@Service // service로 인식시켜 줌
public class KakaoService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String restApi = "fd5889d7ba3ad91e09594fa6e61b7470";
    private final String adminKey = "c2d90853a18a7b6b9024b724780065a9";

    public String kakaoConnect(){
        try {
            StringBuffer url = new StringBuffer();
            url.append("https://kauth.kakao.com/oauth/authorize?");
            url.append("client_id=").append(restApi);
            url.append("&redirect_uri=").append("http://localhost:8080/kakao/callback");
            url.append("&response_type=" + "code");

            // https://kauth.kakao.com/oauth/authorize : Get 요청할 링크
            // ? : 뒤에 매개변수를 넣어줌
            // client_id : 클라이언트(고객)의 id (매개변수 이름)
            // = : 대입
            // f12393a3d014f5b41c1891bca7f2c800 : REST API 키 (매개변수 값)
            // & : 그리고
            // redirect_uri : 값을 보낼 url 링크 (매개변수 이름)
            // = : 대입
            // http://localhost:8080/kakao/callback : redirect_uri (kakao delvelopers에 미리 등록)(매개변수 값)
            // & : 그리고
            // response_type : response(결과값) 데이터 타입 (매개변수 이름)
            // = : 대입
            // code : 코드 타입 (매개변수 값)
            return url.toString();
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public String kakaoAutoConnect(){
        try {
            StringBuffer url = new StringBuffer();
            url.append(kakaoConnect());
            url.append("&prompt=" + "login");

            return url.toString();
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public JsonNode getKakaoAccessToken(String code) {
        // 요청 보낼 링크(토큰 얻기)
        final String requestUrl = "https://kauth.kakao.com/oauth/token";
        // 매개변수와 값의 리스트
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "authorization_code"); // 인증 타입 (고정값임)
        parameters.add("client_id", restApi); // REST API KEY
        parameters.add("redirect_uri", "http://localhost:8080/kakao/callback"); // 리다이렉트 URI
        parameters.add("code", code); // 인가 코드

        final ResponseEntity<JsonNode> response = kakaoPost(requestUrl, null, parameters);

        return response.getBody();
    }

    public void setCookie(HttpServletResponse res, String name, String value){
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(3600);
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse res, String name){
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    @Transactional
    // kakaoConnect의 결과값(인가코드)가 아래의 매개변수 code로 들어감
    public String kakaoLogin(String code, HttpServletResponse res){
        try {
            // 인카코드에 있는 토큰을 추출
            JsonNode token = getKakaoAccessToken(code);
            String access_token = token.get("access_token").asText();
            // Bear 넣어야 할지도?
            String refresh_token = token.get("refresh_token").asText();

            setCookie(res, "token", access_token);
            setCookie(res, "platform", "kakao");

            // 로그인한 클라이언트의 사용자 정보를 json 타입으로 획득
            User user = kakaoJoin(access_token);
            user.setToken(access_token, refresh_token);
            userRepository.save(user);
            saveUserId(user.getId(), access_token);
            return "/";
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw new Exception401("인증되지 않음.");
        }
    }

    public void saveUserId(Long id, String access_token){
        final String requestUrl = "https://kapi.kakao.com/v1/user/update_profile";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + access_token);
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("properties", "{\"web_id\":\"" + id + "\"}");
        kakaoPost(requestUrl, headers, parameters);
    }

    public boolean checkEmail(String email){
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmail(email);
        return users.isPresent();
    }

    @Transactional
    public User kakaoJoin(String access_token) {
        try {
            User userInfo = makeUserFromKakao(access_token);
            String email = userInfo.getEmail();
            if (!checkEmail(email)) {
                return userRepository.save(userInfo);
            }
            return userRepository.findByEmail(email).orElseThrow(
                    () -> new Exception401("인증되지 않았습니다."));
        } catch (Exception e) {
            throw new Exception500(e.getMessage());
        }
    }

    public User makeUserFromKakao(String access_token){
        JsonNode userInfo = getKakaoUser(access_token);
        JsonNode kakao_account = userInfo.path("kakao_account");
        String encodedPassword = passwordEncoder.encode(access_token);
        JsonNode properties = userInfo.path("properties");
        StringArrayConverter stringArrayConverter = new StringArrayConverter();

        User user = User.builder()
                .email(kakao_account.path("email").asText())
                .password(encodedPassword)
                .name(properties.path("nickname").asText())
                .phoneNumber("01012341234")
                .roles(stringArrayConverter.convertToEntityAttribute("ROLE_USER"))
                .platform("kakao")
                .build();

        return user;
    }

    public JsonNode getKakaoUser(String access_token) {
        final String requestUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + access_token);
        ResponseEntity<JsonNode> response = kakaoPost(requestUrl, headers, null);

        return response.getBody();
    }

    @Transactional
    public void kakaoLogout(Long id, HttpServletResponse res){
        final String requestUrl = "https://kapi.kakao.com/v1/user/logout";
        try{
            User user = userRepository.findById(id).orElseThrow();
            String access_token = user.getAccess_token();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            headers.set("Authorization", "Bearer " + access_token);
            kakaoPost(requestUrl, headers, null);
            user.setToken(null, null);
            deleteCookie(res, "token");
            deleteCookie(res, "platform");
        }
        catch (Exception500 e){
            System.out.println(e.getMessage());
            throw new Exception500("로그아웃 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    public String kakaoFullLogout(Long id, HttpServletResponse res) {
        try{
            User user = userRepository.findById(id).orElseThrow(
                    () -> new Exception401("로그인된 사용자를 찾을 수 없습니다."));
            user.setToken(null, null);
            deleteCookie(res, "token");
            deleteCookie(res, "platform");

            StringBuffer url = new StringBuffer();
            url.append("https://kauth.kakao.com/oauth/logout?");
            url.append("client_id=").append(restApi);
            url.append("&logout_redirect_uri=" + "http://localhost:8080/");

            return url.toString();
        }
        catch (Exception500 e){
            throw new Exception500("로그아웃 도중 오류 발생");
        }
    }

    public void kakaoDisconnect(Long id){
        final String requestUrl = "https://kapi.kakao.com/v1/user/unlink";
        String access_token = userRepository.findById(id).orElseThrow().getAccess_token();

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            kakaoPost(requestUrl, headers, null);
        }
        catch (Exception500 e){
            throw new Exception500("연결 해제 도중 오류 발생");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void kakaoUserList(){
        final String requestUrl = "https://kapi.kakao.com/v1/user/ids";

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + adminKey);
            headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            final ResponseEntity<JsonNode> response = userGet(requestUrl, headers, null);
            if (response.getBody() == null) throw new Exception();
            JsonNode returnNode = response.getBody();

            System.out.print("id : ");
            for (JsonNode id : returnNode.get("elements")){
                System.out.print(id.asText() + " ");
            }
        }
        catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public <T> ResponseEntity<JsonNode> userGet(String requestUrl, HttpHeaders headers, T body){
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, JsonNode.class);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public <T> ResponseEntity<JsonNode> kakaoPost(String requestUrl, HttpHeaders headers, T body){
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, JsonNode.class);
        } catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    public void endServer(){
        System.exit(0);
    }
}
