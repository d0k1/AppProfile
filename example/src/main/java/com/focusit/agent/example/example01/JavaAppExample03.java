package com.focusit.agent.example.example01;

import com.focusit.agent.example.example01.classes.*;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Denis V. Kirpichenkov on 24.03.15.
 */
public class JavaAppExample03 {

	public static void main(String[] args) throws IOException {

		Runnable work = new Runnable() {
			@Override
			public void run() {
				Class01 c01 = new Class01();
				Class02 c02 = new Class02();
				Class03 c03 = new Class03();
				Class04 c04 = new Class04();
				Class05 c05 = new Class05();
				Class06 c06 = new Class06();
				Class07 c07 = new Class07();
				Class08 c08 = new Class08();
				Class09 c09 = new Class09();
				Class10 c10 = new Class10();

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));

				c01.m0((int) (Math.random() * 100));
				c02.m1((int) (Math.random() * 100));
				c03.m2((int) (Math.random() * 100));
				c04.m3((int) (Math.random() * 100));
				c05.m4((int) (Math.random() * 100));
				c06.m5((int) (Math.random() * 100));
				c07.m6((int) (Math.random() * 100));
				c08.m7((int) (Math.random() * 100));
				c09.m8((int) (Math.random() * 100));
				c10.m9((int) (Math.random() * 100));
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(5);

		for(int i=0;i<100000;i++){
			executor.submit(work);
		}
		System.in.read();
		executor.shutdown();
	}
}
