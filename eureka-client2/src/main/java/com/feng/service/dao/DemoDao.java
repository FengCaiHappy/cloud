package com.feng.service.dao;


import com.feng.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DemoDao {

    List<UserInfo> list();
}
