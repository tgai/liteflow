package com.yomahub.liteflow.test.component.cmp1;

import com.yomahub.liteflow.core.NodeComponent;

import java.util.Objects;


public class ACmp extends NodeComponent {
    @Override
    public void process() {
        System.out.println("AComp executed!");
        this.getSlot().setResponseData("AComp executed!");
    }

    @Override
    public boolean isAccess() {
        Integer requestData = this.getSlot().getRequestData();
        if (Objects.nonNull(requestData) && requestData > 100){
            return true;
        }
        System.out.println("AComp isAccess false.");
        return false;
    }
}
