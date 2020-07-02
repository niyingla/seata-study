package icu.funkye.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.funkye.entity.Test;
import icu.funkye.mapper.TestMapper;
import icu.funkye.service.ITestService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, Test> implements ITestService {

    @Override
    @Transactional
    public Object Commit() {
        log.info("seata分布式事务Id:{}", RootContext.getXID());
        update(Wrappers.<Test>lambdaUpdate().eq(Test::getId,1).setSql("two=two+1"));
        return true;
    }

    @Transactional
    @Override
    public Object create() {
        log.info("seata分布式事务Id:{}", RootContext.getXID());
        Test test = new Test();
        test.setOne(String.valueOf(Math.random() * 10000));
        test.setTwo((int) (Math.random() * 10000));
        test.setCreateTime(LocalDateTime.now());
        saveOrUpdate(test);
        return true;
    }
}
