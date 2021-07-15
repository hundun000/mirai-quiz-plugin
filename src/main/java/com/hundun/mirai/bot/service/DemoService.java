package com.hundun.mirai.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hundun.mirai.bot.db.DemoRepository;

import net.mamoe.mirai.message.data.Message;

/**
 * @author hundun
 * Created on 2021/07/01
 */
@Service
public class DemoService {
    @Autowired
    DemoRepository repository;
    
    public String check() {
        return "my repository " + (repository == null ? "is " : "is not ") + "null";
    }

    public String work() {
        return "Hello world, repository has " + repository.count() + " document(s).";
    }
}
