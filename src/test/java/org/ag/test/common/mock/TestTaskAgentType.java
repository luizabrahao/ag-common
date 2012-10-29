package org.ag.test.common.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.ag.common.agent.TaskAgentType;
import org.ag.common.task.Task;
import org.ag.common.task.WandererTask;

@ThreadSafe
public enum TestTaskAgentType implements TaskAgentType {
	TYPE;
	
	private final String name = "agent:type:wandering-test";
	private final List<Task> tasks;
	
	private TestTaskAgentType() {
		tasks = new ArrayList<Task>();
		tasks.add(new WandererTask());
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}
}
