package com.example.demo.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class UserResponse {
    @AllArgsConstructor
    @Setter
    @Getter
    public static class UserDto{

        private Long id;
        private String email;
        private String name;
        private String phoneNumber;
        private String access_token;
        private String platform;

        public User toEntity(){
            return User.builder()
                    .id(id)
                    .email(email)
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .access_token(access_token)
                    .platform(platform)
                    .build();
        }

        public static UserDto toUserDto(User user){
            return new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPhoneNumber(),
                    user.getAccess_token(),
                    user.getPlatform());
        }
    }
}
