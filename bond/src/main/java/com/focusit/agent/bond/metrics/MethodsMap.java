package com.focusit.agent.bond.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class MethodsMap {
	private static MethodsMap instance = new MethodsMap();

	private final List<String> methods;
	private final ConcurrentHashMap<String, Long> methodIndexes;
	private final AtomicLong lastIndex;

	private final static int INITIAL_SIZE=15000;

	protected MethodsMap(){
		methods = new ArrayList<>(INITIAL_SIZE);
		methodIndexes = new ConcurrentHashMap<>(INITIAL_SIZE);
		lastIndex = new AtomicLong(0);
	}

	public static MethodsMap getInstance(){
		return instance;
	}

	public long addMethod(String method){
		Long current = methodIndexes.get(method);

		if(current!=null){
			return current.longValue();
		}

		methods.add(method);
		long index = lastIndex.getAndIncrement();
		methodIndexes.put(method, index);

		System.out.println("mappedMethod: "+method+" = "+index);
		return index;
	}

	public long getMethodIndex(String method){
		Long result = methodIndexes.get(method);
		return result==null?-1:result;
	}
}
