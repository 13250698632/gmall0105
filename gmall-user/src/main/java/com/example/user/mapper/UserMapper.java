package com.example.user.mapper;

import com.example.user.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {
    List<UmsMember> selectAllUser();
}
