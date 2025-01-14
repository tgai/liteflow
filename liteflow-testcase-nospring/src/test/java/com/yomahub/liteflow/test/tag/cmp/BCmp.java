/**
 * <p>Title: liteflow</p>
 * <p>Description: 轻量级的组件式流程框架</p>
 * @author Bryan.Zhang
 * @email weenyc31@163.com
 * @Date 2020/4/1
 */
package com.yomahub.liteflow.test.tag.cmp;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.entity.data.Slot;

public class BCmp extends NodeComponent {

	@Override
	public void process() {
		Slot slot = this.getSlot();
		ConcurrentHashSet<String> testSet = slot.getData("test");
		testSet.add(this.getTag());
	}
}
