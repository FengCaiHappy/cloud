package com.feng.entity;

import com.feng.autoinjection.autoannotation.FTableName;
import lombok.Data;

import java.util.Date;

@Data
@FTableName(tableName="userInfo")
public class UserInfo {

    private Integer id;

    private String name;

    private Integer age;

    private Date birthday;
}
