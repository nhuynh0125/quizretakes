package quizretakes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Jeff Offutt Date: January, 2019
 *
 *         Wiring the pieces together: quizschedule.java -- Servlet entry point
 *         for students to schedule quizzes quizReader.java -- reads XML file
 *         and stores in quizzes. Used by quizschedule.java quizzes.java -- A
 *         list of quizzes from the XML file Used by quizschedule.java
 *         quizBean.java -- A simple quiz bean Used by quizzes.java and
 *         readQuizzesXML.java retakesReader.java -- reads XML file and stores
 *         in retakes. Used by quizschedule.java retakes.java -- A list of
 *         retakes from the XML file Used by quizschedule.java retakeBean.java
 *         -- A simple retake bean Used by retakes.java and readRetakesXML.java
 *         apptBean.java -- A bean to hold appointments
 * 
 *         quizzes.xml -- Data file of when quizzes were given retakes.xml --
 *         Data file of when retakes are given
 */

public class quizschedule {
	// Data files
	// location maps to /webapps/offutt/WEB-INF/data/ from a terminal window.

	// change dataLocation to your own file directory
	private static final String dataLocation = "/Users/ryanvo1/Documents/SWE 437/quizretakes/";
	static private final String separator = ",";
	private static final String courseBase = "course";
	private static final String quizzesBase = "quiz-orig";
	private static final String retakesBase = "quiz-retakes";
	private static final String apptsBase = "quiz-appts";
	private static final String password = "password"; //for professor use only

	// Filenames to be built from above and the courseID parameter
	private String courseFileName;
	private String quizzesFileName;
	private String retakesFileName;
	private String apptsFileName;

	// Passed as parameter and stored in course.xml file (format: "swe437")
	private static String courseID;
	// Stored in course.xml file, default 14
	// Number of days a retake is offered after the quiz is given
	private int daysAvailable = 14;

	// To be set by getRequestURL()
	private String thisServlet = "";

// doGet() : Prints the form to schedule a retake
	public void doGet(String courseID) {
		// what is printewriter for?
		// PrintWriter out = response.getWriter();

		if (courseID != null && !courseID.isEmpty()) {
			courseBean course;
			courseReader cr = new courseReader();
			courseFileName = dataLocation + courseBase + "-" + courseID + ".xml";
			// courseFileName = courseBase + "-" + courseID + ".xml";
			try {
				course = cr.read(courseFileName);
			} catch (Exception e) {
				String message = "Can't find the data files for course ID " + courseID + ". You can try again.";
				return;
			}
			daysAvailable = Integer.parseInt(course.getRetakeDuration());

			// Filenames to be built from above and the courseID
			String quizzesFileName = dataLocation + quizzesBase + "-" + courseID + ".xml";
			String retakesFileName = dataLocation + retakesBase + "-" + courseID + ".xml";
			String apptsFileName = dataLocation + apptsBase + "-" + courseID + ".txt";

			// Load the quizzes and the retake times from disk
			quizzes quizList = new quizzes();
			retakes retakesList = new retakes();
			quizReader qr = new quizReader();
			retakesReader rr = new retakesReader();

			try { // Read the files and print the form
				quizList = qr.read(quizzesFileName);
				retakesList = rr.read(retakesFileName);
				// printQuizScheduleForm(out, quizList, retakesList, course);
				printQuizScheduleForm(quizList, retakesList, course);

			} catch (Exception e) {
				String message = "Can't find the data files for course ID " + courseID + ". You can try again.";
			}
		} 
	}

	/*
	 * courseID is input taken from user
	 * studentName is input taken from user inside of printQuizSchedule?
	 * allIDs is an array of string with quizzes that the user selects
	 */
	public void doPost(String courseID, String studentName, String[] allIDs) throws IOException {
		// No saving if IOException
		boolean IOerrFlag = false;
		String IOerrMessage = "";

		// Filename to be built from above and the courseID
		String apptsFileName = dataLocation + apptsBase + "-" + courseID + ".txt";

		// Get name and list of retake requests from parameters
		// String studentName = request.getParameter("studentName");
		// String[] allIDs = request.getParameterValues("retakeReqs");


		if (allIDs != null && studentName != null && studentName.length() > 0) {
			// Append the new appointment to the file
			try {
				File file = new File(apptsFileName);
				synchronized (file) { // Only one student should touch this file at a time.
					if (!file.exists()) {
						file.createNewFile();
					}
					FileWriter fw = new FileWriter(file.getAbsoluteFile(), true); // append mode
					BufferedWriter bw = new BufferedWriter(fw);

					for (String oneIDPair : allIDs) {
						//bw.write(oneIDPair + separator + studentName + "\n");
						bw.write(oneIDPair + separator);
					}
					bw.write(studentName + "\n");

					bw.flush();
					bw.close();
				} // end synchronize block
			} catch (IOException e) {
				IOerrFlag = true;
				IOerrMessage = "I failed and could not save your appointment." + e;
			}

			// Respond to the student
			if (IOerrFlag) {
				System.out.println(IOerrMessage);
			} else {
				if (allIDs.length == 1)
					System.out.println(studentName + ", your appointment has been scheduled.");
				else
					System.out.println(studentName + ", your appointments have been scheduled.");
				System.out.println("Please arrive in time to finish the quiz before the end of the retake period.");
				System.out.println("If you cannot make it, please cancel by sending email to your professor.");
			}

		} else { // allIDs == null or name is null
			if (allIDs == null)
				System.out.println("You didn't choose any quizzes to retake.");
			if (studentName == null || studentName.length() == 0)
				System.out.println("You didn't give a name ... no anonymous quiz retakes.");

		}
			}

	//print out the quiz schedule
	public void printQuizScheduleForm(quizzes quizList, retakes retakesList, courseBean course) {
		boolean skip = false;
		LocalDate startSkip = course.getStartSkip();
		LocalDate endSkip = course.getEndSkip();

		boolean retakePrinted = false;

		System.out.println("\nGMU quiz retake scheduler for class " + course.getCourseTitle());

		// print the main form
		// System.out.println("<form name='quizSchedule' method='post' action='" +
		// thisServlet + "?courseID=" + courseID + "' >");
		System.out.print(
				"\nYou can sign up for quiz retakes within the next two weeks. Enter your name (as it appears on the class roster), then select which date, time, and quiz you wish to retake from the following list.");

		LocalDate today = LocalDate.now();
		LocalDate endDay = today.plusDays(new Long(daysAvailable));
		LocalDate origEndDay = endDay;
		// if endDay is between startSkip and endSkip, add 7 to endDay
		if (!endDay.isBefore(startSkip) && !endDay.isAfter(endSkip)) { // endDay is in a skip week, add 7 to endDay
			endDay = endDay.plusDays(new Long(7));
			skip = true;
		}

		System.out.println("\nToday is ");
		System.out.println((today.getDayOfWeek()) + ", " + today.getMonth() + " " + today.getDayOfMonth());
		System.out.println("\nCurrently scheduling quizzes for the next two weeks, until ");
		System.out.println((endDay.getDayOfWeek()) + ", " + endDay.getMonth() + " " + endDay.getDayOfMonth());

		for (retakeBean r : retakesList) {
			LocalDate retakeDay = r.getDate();
			if (!(retakeDay.isBefore(today)) && !(retakeDay.isAfter(endDay))) {
				// if skip && retakeDay is after the skip week, print a white bg message
				if (skip && retakeDay.isAfter(origEndDay)) { // A "skip" week such as spring break.
					// format skip week
					System.out.println("\nSkipping a week, no quiz or retakes.");
					// Just print for the FIRST retake day after the skip week
					skip = false;
				}
				retakePrinted = true;
				// format: Friday, January 12, at 10:00am in EB 4430
				System.out.println("\n" + retakeDay.getDayOfWeek() + ", " + retakeDay.getMonth() + " "
						+ retakeDay.getDayOfMonth() + ", at " + r.timeAsString() + " in " + r.getLocation());

				for (quizBean q : quizList) {
					LocalDate quizDay = q.getDate();
					LocalDate lastAvailableDay = quizDay.plusDays(new Long(daysAvailable));
					// To retake a quiz on a given retake day, the retake day must be within two
					// ranges:
					// quizDay <= retakeDay <= lastAvailableDay --> (!quizDay > retakeDay) &&
					// !(retakeDay > lastAvailableDay)
					// today <= retakeDay <= endDay --> !(today > retakeDay) && !(retakeDay >
					// endDay)

					if (!quizDay.isAfter(retakeDay) && !retakeDay.isAfter(lastAvailableDay) && !today.isAfter(retakeDay)
							&& !retakeDay.isAfter(endDay)) {
						System.out.println("\nQuiz " + q.getID() + " from " + quizDay.getDayOfWeek() + ", "
								+ quizDay.getMonth() + " " + quizDay.getDayOfMonth());
					}
				}
			}
			if (retakePrinted) {
				retakePrinted = false;
			}
		}
		System.out.println("\nAll quiz retake opportunities");
		for (retakeBean r : retakesList) {
			System.out.print("\n" + r);
		}

		// get name
		System.out.println("\nEnter your name (first and last name): ");
		String studentName = getInput();
		// get selections
		System.out.print("Select quiz retake opportunities (put space between retakeID and quizID): ");

		ArrayList<Integer> selections = new ArrayList<Integer>();
		getSelections(selections);
		
		String[] retakeOpps = new String[selections.size()];

		int j = 0;
		for(int i = 0; i < selections.size(); i++) {
			retakeOpps[j] = selections.get(i).toString();
			j++;
		}

		try {
			doPost(courseID, studentName, retakeOpps);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	//read the retakeID and quizID entered by the user
	public void getSelections(ArrayList<Integer> selections) {
		String input = getInput();
		Scanner kb = new Scanner(input);

		while (kb.hasNext()) {
			selections.add(kb.nextInt());
		}

	}

	//read input from the user
	public String getInput() {
		Scanner kb = new Scanner(System.in);
		return kb.nextLine();
	}
	
	@SuppressWarnings("unchecked")
	//display all appointments on CL
	public void displayAllAppointments(){
	
		apptsFileName = dataLocation + apptsBase + "-" + courseID + ".txt";
		retakesFileName = dataLocation + retakesBase + "-" + courseID + ".xml";
		
		ArrayList<apptBean> appts = new ArrayList<apptBean>();
		retakes retakesList = new retakes();
		
		//read all appointments from the output file
		try{
			apptsReader ar = new apptsReader();
			appts = ar.read(apptsFileName);
			
			retakesReader rr = new retakesReader();
			retakesList = rr.read(retakesFileName);
		} catch (Exception e) {
			String message = "Can't find the data files for course ID " + courseID + ". You can try again.";
		}
		
		//print each appointment at a time
		for(int i = 0; i < appts.size(); i++){
		
			apptBean newAppt = appts.get(i);
			
			//print student's name and quizID first
			System.out.println("Student's name: " + newAppt.getName()); 
			System.out.println("Quiz " + newAppt.getQuizID());
			
			int retakeID = newAppt.getRetakeID();
			
			//print information associated with the retakeID
			int j = 1;
			int k = 0;
			for(retakeBean r : retakesList){
				if(j == retakeID){
					System.out.println("Session: " + r.toString() + "\n");
					k++;
				}
				j++;
			}
		}
	}

	public static void main(String[] args) {

		quizschedule q = new quizschedule();
		Scanner kb = new Scanner(System.in);

		System.out.print("Enter course ID: ");
		q.courseID = kb.next();
		
		int input = 0;
		
		do{
			System.out.println("(For student use) Enter 1 to schedule a new quiz retake:");
			System.out.println("(For professor use) Enter 2 to see all quiz retake appointments:");
			
			input = kb.nextInt();
			
			if(input == 1){
				q.doGet(courseID);
			}
			else if(input == 2){
				System.out.println("Please enter the password: ");
				String password = kb.next();
				//check password here
				if(password.equals(q.password)){
					q.displayAllAppointments();
				}
				else{
					System.out.println("Wrong password! Failed to log in.");
				}
			}
			else{
				System.out.println("Invalid input! Try again.\n");
			}	
		}while(input != 1 && input != 2);
	}
}