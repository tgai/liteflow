package com.yomahub.liteflow.builder;

import cn.hutool.core.collection.CollectionUtil;
import com.yomahub.liteflow.entity.data.DataBus;
import com.yomahub.liteflow.entity.flow.*;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.FlowBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain基于代码形式的组装器
 * @author Bryan.Zhang
 * @since 2.6.8
 */
public class LiteFlowChainBuilder {

    private Chain chain;

    //声明这个变量，而不是用chain.getConditionList的目的，是为了辅助平滑加载
    //虽然FlowBus里面的map都是CopyOnWrite类型的，但是在buildCondition的时候，为了平滑加载，所以不能事先把chain.getConditionList给设为空List
    //所以在这里做一个缓存，等conditionList全部build完毕后，再去一次性替换chain里面的conditionList
    private final List<Condition> conditionList;

    public static LiteFlowChainBuilder createChain(){
        return new LiteFlowChainBuilder();
    }

    public LiteFlowChainBuilder(){
        chain = new Chain();
        conditionList = new ArrayList<>();
    }

    //在parser中chain的build是2段式的，因为涉及到依赖问题，以前是递归parser
    //2.6.8之后取消了递归的模式，两段式组装，先把带有chainName的chain对象放进去，第二段再组装chain里面的condition
    //所以这里setChainName的时候需要判断下
    public LiteFlowChainBuilder setChainName(String chainName){
        if (FlowBus.containChain(chainName)){
            this.chain = FlowBus.getChain(chainName);
        }else{
            this.chain.setChainName(chainName);
        }
        return this;
    }

    public LiteFlowChainBuilder setCondition(Condition condition){
        //这里把condition组装进conditionList，
        buildConditions(condition);
        return this;
    }

    public void build(){
        this.chain.setConditionList(this.conditionList);
        FlowBus.addChain(this.chain);
    }

    private void buildConditions(Condition condition) {
        //这里进行合并逻辑
        //对于then来说，相邻的2个then会合并成一个condition
        //对于when来说，相同组的when会合并成一个condition，不同组的when还是会拆开
        if (condition.getConditionType().equals(ConditionTypeEnum.TYPE_PRE)) {
            this.conditionList.add(new PreCondition(condition));
        } else if (condition.getConditionType().equals(ConditionTypeEnum.TYPE_THEN)) {
            if (this.conditionList.size() >= 1 &&
                    CollectionUtil.getLast(this.conditionList) instanceof ThenCondition) {
                CollectionUtil.getLast(this.conditionList).getNodeList().addAll(condition.getNodeList());
            } else {
                this.conditionList.add(new ThenCondition(condition));
            }
        } else if (condition.getConditionType().equals(ConditionTypeEnum.TYPE_WHEN)) {
            if (this.conditionList.size() > 1 &&
                    CollectionUtil.getLast(this.conditionList) instanceof WhenCondition &&
                    CollectionUtil.getLast(this.conditionList).getGroup().equals(condition.getGroup())) {
                CollectionUtil.getLast(this.conditionList).getNodeList().addAll(condition.getNodeList());
            } else {
                this.conditionList.add(new WhenCondition(condition));
            }
        } else if (condition.getConditionType().equals(ConditionTypeEnum.TYPE_FINALLY)) {
            this.conditionList.add(new FinallyCondition(condition));
        }

        //每一次build之后，对conditionList进行排序，pre最前面，finally最后
        //这里为什么要排序，因为在声明的时候，哪怕有人不把pre放最前，finally放最后，但最终也要确保是正确的顺序
        CollectionUtil.sort(this.conditionList, (o1, o2) -> {
            if (o1.getConditionType().equals(ConditionTypeEnum.TYPE_PRE) || o2.getConditionType().equals(ConditionTypeEnum.TYPE_FINALLY)){
                return -1;
            } else if (o2.getConditionType().equals(ConditionTypeEnum.TYPE_PRE) || o1.getConditionType().equals(ConditionTypeEnum.TYPE_FINALLY)){
                return 1;
            }
            return 0;
        });
    }
}
