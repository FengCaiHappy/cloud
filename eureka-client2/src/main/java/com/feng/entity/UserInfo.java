package com.feng.entity;

import com.feng.autoinjection.autoannotation.FTableName;
import lombok.Data;

@Data
@FTableName(name="userInfo")
public class UserInfo {

    private Integer id;

    private String name;

    private Integer age;
}
