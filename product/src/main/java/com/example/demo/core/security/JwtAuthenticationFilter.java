package com.example.demo.core.security;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.user.StringArrayConverter;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager){
        super(authenticationManager);
    }

    // ** Http 요청이 발생할 때마다 호출되는 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String prefixJwt = request.getHeader(JwtTokenProvider.HEADER);
        String platform = request.getHeader(JwtTokenProvider.PLATFORM);

        // ** 헤더가 없다면 이 메소드에서 더 할 일은 없음, 다음으로 넘김.
        if (prefixJwt == null){
            chain.doFilter(request, response);
            return;
        }

        // ** Bearer 제거
        String jwt = prefixJwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");
        try {
            log.debug("토큰 있음.");

            CustomUserDetails customUserDetails = null;

            if (platform.equals("kakao")) {
                JsonNode userInfo = getKakaoUserInfo(jwt);
                String email = userInfo.path("kakao_account").path("email").asText();

                StringArrayConverter stringArrayConverter = new StringArrayConverter();
                List<String> rolesList = stringArrayConverter.convertToEntityAttribute("ROLE_USER");

                User user = User.builder()
                        .email(email)
                        .roles(rolesList)
                        .build();
                customUserDetails = new CustomUserDetails(user);
            } else {
                // ** 토큰 검증
                DecodedJWT decodedJWT = JwtTokenProvider.verify(jwt);

                if (Blacklist.isTokenBlacklisted(jwt)) {
                    throw new Exception401("사용불가능한 토큰입니다.");
                }

                // ** 사용자 정보 추출
                Long id = decodedJWT.getClaim("id").asLong();
                String roles = decodedJWT.getClaim("roles").asString();

                // ** 권한 정보를 문자열 리스트로 변환
                StringArrayConverter stringArrayConverter = new StringArrayConverter();
                List<String> rolesList = stringArrayConverter.convertToEntityAttribute(roles);

                // ** 추출한 정보로 User를 생성
                User user = User.builder()
                        .id(id)
                        .roles(rolesList)
                        .build();
                customUserDetails = new CustomUserDetails(user);
            }

            // ** Spring Security / 인증 정보를 관리하는데 사용
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    customUserDetails.getPassword(),
                    customUserDetails.getAuthorities()
            );

            // ** SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("인증 객체 생성");
        }
        catch (SignatureVerificationException sve) {
            log.debug("토큰 검증 실패");
        } catch (TokenExpiredException tee){
            log.debug("토큰 사용 만료");
        } finally {
            // ** 필터로 응답을 넘긴다.
            chain.doFilter(request, response);
        }
    }

    public JsonNode getKakaoUserInfo(String access_token) {
        final String requestUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + access_token);
        ResponseEntity<JsonNode> response = kakaoPost(requestUrl, headers, null);

        return response.getBody();
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
}
