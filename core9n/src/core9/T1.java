package core9;

import java.util.List;

public class T1 {

	public static void main(String[] args) {

		List<Integer> l = getL();
		
		l.forEach(System.out::println);
	}

	public static List<Integer> getL() {
		List<Integer> l = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		return l;
	}

}
