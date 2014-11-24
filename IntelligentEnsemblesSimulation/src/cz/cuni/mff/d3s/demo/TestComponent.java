package cz.cuni.mff.d3s.demo;

import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;

@Component
public class TestComponent {
	
	public Integer myId;
	
	public Integer ensembleId;
	
	public Integer groupId;
	
	public Integer counter;
	
	public TestComponent(int myId, int ensembleId, int groupId) {
		this.myId = myId;
		this.ensembleId = ensembleId;
		this.groupId = groupId;
		counter = 0;
		System.out.println("Created component " + myId + ", groupId=" + groupId + ", ensembleId=" + ensembleId);
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void DoCount(
		@In("myId") Integer myId,
		@InOut("counter") ParamHolder<Integer> counter) {
		counter.value = counter.value + 1;
		System.out.println("Component " + myId + ", counter=" + counter.value);
	}
	
}
