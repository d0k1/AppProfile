package com.focusit.agent.example.multithread;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Denis V. Kirpichenkov on 02.12.14.
 */
public class ClassOne {
	public void doSomeWhiteNoise(){
		int random = randInt(10, 20);
		BigInteger result = new BigInteger(""+random);
		for(int i=0;i<random;i++)
		{
				result.multiply(new BigInteger(""+random).pow(3));
		}

		System.out.println("Thread "+Thread.currentThread().getId()+" result "+result.toString());
	}

	private int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
}
