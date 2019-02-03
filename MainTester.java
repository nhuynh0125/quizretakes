package quizretakes;

import java.util.Scanner;

public class MainTester {

	public static void main(String[] args) {

		quizschedule q = new quizschedule();
		Scanner kb = new Scanner(System.in);

		System.out.println("Enter course ID: ");
		String courseID = kb.next();

		q.doGet(courseID);

		// allIDs is an array of strings that are read when the user selects them
		// q.doPost(courseID, "sTuDeNt nAmE", allIDs);
	}

}
