package com.fishtripplanner.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class JoinRequest {
    private String username;
    private String password;
    private String email;
    private String address;
    private String name;
    private String nickname;
    private String phonenumber;
    private String gender;
    private String age;
    private String birthyear;
    private String birthday;

    // 아래는 사업자 전용 필드
    private String businessNumber; // ← "company" input name과 일치하게
    private String serviceType;    // ← "service"
    private String companyName;
}

