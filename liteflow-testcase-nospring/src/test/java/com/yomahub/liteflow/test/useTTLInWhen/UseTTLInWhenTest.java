package com.yomahub.liteflow.test.useTTLInWhen;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.FlowExecutorHolder;
import com.yomahub.liteflow.entity.data.DefaultSlot;
import com.yomahub.liteflow.entity.data.LiteflowResponse;
import com.yomahub.liteflow.property.LiteflowConfig;
import com.yomahub.liteflow.test.BaseTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 在when异步节点的情况下去拿ThreadLocal里的测试场景
 * @author Bryan.Zhang
 * @since 2.6.3
 */
public class UseTTLInWhenTest extends BaseTest {

    private static FlowExecutor flowExecutor;

    @BeforeClass
    public static void init(){
        LiteflowConfig config = new LiteflowConfig();
        config.setRuleSource("useTTLInWhen/flow.xml");
        flowExecutor = FlowExecutorHolder.loadInstance(config);
    }

    @Test
    public void testUseTTLInWhen() throws Exception{
        LiteflowResponse<DefaultSlot> response = flowExecutor.execute2Resp("chain1", "arg");
        Assert.assertEquals("hello,b", response.getSlot().getData("b"));
        Assert.assertEquals("hello,c", response.getSlot().getData("c"));
        Assert.assertEquals("hello,d", response.getSlot().getData("d"));
        Assert.assertEquals("hello,e", response.getSlot().getData("e"));
        Assert.assertEquals("hello,f", response.getSlot().getData("f"));
    }
}
