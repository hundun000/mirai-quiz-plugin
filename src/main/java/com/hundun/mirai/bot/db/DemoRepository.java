package com.hundun.mirai.bot.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hundun
 * Created on 2021/07/01
 */
@Repository
public interface DemoRepository extends MongoRepository<DemoDocument, String>{
    
}
