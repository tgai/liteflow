package com.yomahub.liteflow.test.aop;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.entity.data.DefaultSlot;
import com.yomahub.liteflow.entity.data.LiteflowResponse;
import com.yomahub.liteflow.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 切面场景单元测试
 * @author Bryan.Zhang
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:/aop/application-global.xml")
public class CustomAOPSpringTest extends BaseTest {

    @Resource
    private FlowExecutor flowExecutor;

    //测试自定义AOP，串行场景
    @Test
    public void testCustomAopS() {
        LiteflowResponse<DefaultSlot> response= flowExecutor.execute2Resp("chain1", "it's a request");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals("before_after", response.getSlot().getData("a"));
        Assert.assertEquals("before_after", response.getSlot().getData("b"));
        Assert.assertEquals("before_after", response.getSlot().getData("c"));
    }

    //测试自定义AOP，并行场景
    @Test
    public void testCustomAopP() {
        LiteflowResponse<DefaultSlot> response= flowExecutor.execute2Resp("chain2", "it's a request");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals("before_after", response.getSlot().getData("a"));
        Assert.assertEquals("before_after", response.getSlot().getData("b"));
        Assert.assertEquals("before_after", response.getSlot().getData("c"));
    }
}
