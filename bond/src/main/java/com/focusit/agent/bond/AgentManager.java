package com.focusit.agent.bond;

/**
 * Created by Denis V. Kirpichenkov on 05.12.14.
 */
public class AgentManager {

	public void addShutodwnHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Agent.statDump.exit();
					Agent.methodDump.exit();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Agent.methodDump.dumpRest();
				Agent.statDump.dumpRest();
			}
		});
	}
}
