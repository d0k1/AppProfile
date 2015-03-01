package com.focusit.agent.utils.common;

import java.util.ArrayList;

/**
 * Created by Denis V. Kirpichenkov on 02.03.15.
 */
public class BondLongObjectMap<T> {
	private final static int BUCKETS = 512;

	private class BucketItem {
		Object items[] = new Object[BUCKETS];

		public void put(int index, T value){
			items[index] = value;
		}

		public T get(int index){
			return (T) items[index];
		}
	}

	private ArrayList<BucketItem> buckets = new ArrayList<>();

	public BondLongObjectMap(int initialCapacity){
		for(int i=0;i<initialCapacity;i++){
			put(i, null);
		}
	}

	public void clear(){
		buckets.clear();
	}

	public void put(long index, T value){
		int pos = (int) (index / BUCKETS);

		if(buckets.size()<=pos){
			long newItems = pos - buckets.size()+1;
			for(long i=0;i<newItems;i++){
				buckets.add(new BucketItem());
			}
		}
		buckets.get(pos).put((int) (index%BUCKETS), value);
	}

	public T get(long index){
		int pos = (int) (index / BUCKETS);

		if(buckets.size()<=pos) {
			return null;
		}
		return buckets.get(pos).get((int) (index%BUCKETS));
	}

	public void forEach(IterateFunction<T> function){
		long key = 0;

		for(int i=0;i<buckets.size();i++) {
			key = i*BUCKETS;
			for(int j=0;j<BUCKETS;j++){
				key+=j;

				Object item = buckets.get(i).get(j);
				if(item!=null){
					function.process(key, (T) item);
				}
			}
		}
	}

	public interface IterateFunction<T>{
		void process(long key, T value);
	}
}
