package core9;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class T1Test {

	@BeforeAll
	public static void runFirstTime() {
		System.out.println("before ALl");
	}
	
	@BeforeEach
	public void runEachTime() {
		System.out.println("runEachTime");
	}
	
	@Test
	void test1() {
		List<Integer> nums = T1.getL();
		assertTrue(nums.size() >= 10);
	}
	@Test
	void test2() {
		List<Integer> nums = T1.getL();
		assertTrue(nums.size() >= 10);
	}
}
