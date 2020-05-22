package gui;

import java.math.BigInteger;
import java.util.Arrays;

public class Main2 {

	public static void main(String[] args) {
		BigInteger result = BigInteger.ZERO;
		for (int i = 0; i < 251; i++) {
			result = result.add(BigInteger.valueOf((long) Math.pow(2, 20 + (2 * i))));
		}
		System.out.println(result);
		System.out.println(Arrays.toString(BigInteger.valueOf(2363731045961745273L).divideAndRemainder(result)));
	}

}
