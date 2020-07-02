package icu.funkye.controller;

import icu.funkye.entity.Account;
import icu.funkye.entity.Orders;
import icu.funkye.entity.Product;
import icu.funkye.service.IAccountService;
import icu.funkye.service.IOrderService;
import icu.funkye.service.IProductService;
import icu.funkye.service.ITestService;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author funkye
 * @since 2019-03-20
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private IAccountService accountService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IProductService productService;
    @Autowired
    private ITestService testService;
    @Autowired
    private RedisTemplate redisTemplate;

    private Lock lock = new ReentrantLock();


    @PostConstruct
    public void initRedisCount(){
        //设值
        redisTemplate.opsForValue().set("i", 1000);
    }
    /**
     * 简单测试提交分布式事务接口
     *
     * @return
     */
    @GetMapping(value = "seataCommit")
    @GlobalTransactional
    public Object seataCommit() {
        testService.commit();
        int i = 1 / 0;
        return true;
    }

    @GetMapping("seata")
    public Object seata() {
        long l = System.currentTimeMillis();
        System.out.println(l);
        seataCommit();
        long l1 = System.currentTimeMillis();
        System.out.println(l1);
        return true;
    }

    @GlobalTransactional
    @GetMapping("testMS")
    public Object testMS(){
        long l = System.currentTimeMillis();
        miaoSha();
        long l1 = System.currentTimeMillis();
        log.info("事物消费时间【{}】", l1 - l);
        return true;
    }

    public void miaoSha() {
        //减去库存
        Long aLong = redisTemplate.getConnectionFactory().getConnection().decr(redisTemplate.getKeySerializer().serialize("i"));
        //创建订单
        testService.create();

        //测试报错
        if(aLong % 100 == 0){
            int i = 1 / 0;
        }
    }

    /**
     * 秒杀下单分布式事务测试
     * @return
     * @throws TransactionException
     */
    @GetMapping(value = "testCommit")
    @GlobalTransactional
    public Object testCommit() throws TransactionException {
        lock.lock();
        try {
            Product product = productService.getById(1);
            if (product.getStock() > 0) {
                LocalDateTime now = LocalDateTime.now();
                logger.info("seata分布式事务Id:{}", RootContext.getXID());
                Account account = accountService.getById(1);
                Orders orders = new Orders();
                orders.setCreateTime(now);
                orders.setProductId(product.getId());
                orders.setReplaceTime(now);
                orders.setSum(1);
                orders.setAmount(product.getPrice());
                orders.setAccountId(account.getId());
                product.setStock(product.getStock() - 1);
                account.setSum(account.getSum() != null ? account.getSum() + 1 : 1);
                account.setLastUpdateTime(now);
                productService.updateById(product);
                accountService.updateById(account);
                orderService.save(orders);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.info("载入事务{}进行回滚" + e.getMessage(), RootContext.getXID());
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return false;
        } finally {
            lock.unlock();
        }
    }
}
