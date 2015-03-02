package com.focusit.agent.utils.common;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LongObjectRedBlackTreeTest {

	static class TestData {
		public String item = "";
		public TestData(String item){
			this.item = item;
		}
	}

	@Test
	public void testInit(){
		LongObjectRedBlackTree<TestData> map = new LongObjectRedBlackTree<>();
		Assert.assertEquals(map.size(), 0);
	}

	@Test
	public void testBigIndexesSize(){
		LongObjectRedBlackTree<TestData> map = new LongObjectRedBlackTree<>();

		long indexes[] = new long[5];
		indexes[0] = 50000000L;
		indexes[1] = 0L;
		indexes[2] = 10000000000L;
		indexes[3] = 500;
		indexes[4] = 100500;

		for(long index:indexes) {
			map.put(index, new TestData(""+index));
		}

		Assert.assertEquals(map.size(), 5);
	}

	@Test
	public void testBigIndexesData(){
		LongObjectRedBlackTree<TestData> map = new LongObjectRedBlackTree<>();

		long indexes[] = new long[5];
		indexes[0] = 50000000L;
		indexes[1] = 0L;
		indexes[2] = 10000000000L;
		indexes[3] = 500;
		indexes[4] = 100500;

		for(long index:indexes) {
			map.put(index, new TestData(""+index));
		}

		for(long index:indexes) {
			Assert.assertEquals(map.get(index).item, ""+index);
		}
	}

	@Test
	public void testBigIndexesNoData() {
		LongObjectRedBlackTree<TestData> map = new LongObjectRedBlackTree<>();

		Assert.assertNull(map.get(42L));
	}
}
