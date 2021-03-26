package com.luban.controller;

import com.google.common.collect.Lists;
import com.luban.entity.User;
import com.luban.utils.FreeMarkerUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/word")
public class WordExportController {
    @GetMapping("/export")
    public void wordExport(HttpServletResponse response) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "年终总结大会");
        data.put("place", "第一会议室");
        data.put("time", "2021年3月25日");
        data.put("sponsor", "张三");
        List<User> users = Lists.newArrayList(
                new User("张三", "组织部", "10:00"),
                new User("李四", "宣传部", "10:00"));
        data.put("userList", users);
        FreeMarkerUtil.createLocalDoc(data, response);
    }
}
