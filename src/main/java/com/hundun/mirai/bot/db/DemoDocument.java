package com.hundun.mirai.bot.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author hundun
 * Created on 2021/07/01
 */
@Document
public class DemoDocument {
    @Id
    public String id;
    public int data;
}
