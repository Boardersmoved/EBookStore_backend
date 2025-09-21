package com.example.ebookstore_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequestDto {


    @Email(message = "请输入有效的邮箱地址")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email; // 允许修改邮箱，但可能需要验证邮箱唯一性（如果业务允许）

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    private String avatarBase64;

    @Size(max = 1000, message = "个人简介长度不能超过1000个字符") // 假设TEXT对应较长长度
    private String bio;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    @Size(max = 500, message = "常用住址长度不能超过500个字符")
    private String addressText;

}
