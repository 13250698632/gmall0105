package com.lcm.service;

import com.lcm.bean.UmsMember;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    UmsMember login(UmsMember umsMember);
}
