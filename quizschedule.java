// JO 3-Jan-2019
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

//public class quizschedule extends HttpServlet {
public class quizschedule {
	// Data files
	// location maps to /webapps/offutt/WEB-INF/data/ from a terminal window.

	// change dataLocation to your own file directory
	private static final String dataLocation = "/Users/keith/Documents/Spring2019/swe437/hw1/quizretakes/";
	static private final String separator = ",";
	private static final String courseBase = "course";
	private static final String quizzesBase = "quiz-orig";
	private static final String retakesBase = "quiz-retakes";
	private static final String apptsBase = "quiz-appts";

	// Filenames to be built from above and the courseID parameter
	private String courseFileName;
	private String quizzesFileName;
	private String retakesFileName;
	private String apptsFileName;

	// Passed as parameter and stored in course.xml file (format: "swe437")
	private String courseID;
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
				// String message = "<p>Can't find the data files for course ID " + courseID +
				// ". You can try again.";
				// servletUtils.printNeedCourseID(out, thisServlet, message);
			}
		} else {
			// servletUtils.printNeedCourseID(out, thisServlet, "");
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

		// response.setContentType("text/html");
		// PrintWriter out = response.getWriter();
		// servletUtils.printHeader(out);
		// System.out.println("<body bgcolor=\"#DDEEDD\">");

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
						bw.write(oneIDPair + separator + studentName + "\n");
					}

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

			// thisServlet = (request.getRequestURL()).toString();
			// CS server has a flaw--requires https & 8443, but puts http & 8080 on the
			// requestURL
			// thisServlet = thisServlet.replace("http", "https");
			// thisServlet = thisServlet.replace("8080", "8443");
			// System.out.println("<p><a href='" + thisServlet + "?courseID=" + courseID +
			// "'>You can try again if you like.</a>");
		}
		// servletUtils.printFooter(out);
	}

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
						// System.out.println("\n" + q.getID() + "r" + r.getID() + "'>Quiz " + q.getID()
						// + " from " + quizDay.getDayOfWeek() + ", " + quizDay.getMonth() + " " +
						// quizDay.getDayOfMonth());
						// Value is "retakeID:quiziD"
						// System.out.println("\n" + r.getID() + separator + q.getID() + "' id='q" +
						// q.getID() + "r"+ r.getID() + "'>");
						System.out.println("\nQuiz " + q.getID() + " from " + quizDay.getDayOfWeek() + ", "
								+ quizDay.getMonth() + " " + quizDay.getDayOfMonth());
					}
				}
			}
			if (retakePrinted) {
				// System.out.println(" </table>");
				// System.out.println(" <tr><td>");
				retakePrinted = false;
			}
		}
		System.out.println("\nAll quiz retake opportunities");
		for (retakeBean r : retakesList) {
			System.out.print("\n" + r);
		}

		// get name
		System.out.println("\nEnter your name: ");
		String studentName = getInput();
		// get selections
		System.out.print("Select quiz retake opportunities (Put spaces between them): ");

		ArrayList<Integer> selections = new ArrayList<>();
		getSelections(selections);
		String[] retakeOpps = new String[selections.size()];

		int i = 1;
		int j = 0;

		for (retakeBean r : retakesList) {
			if (i == selections.get(j)) {
				retakeOpps[j] = r.toString();
				j++;
			}
			i++;
		}

		try {
			doPost(courseID, studentName, retakeOpps);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getSelections(ArrayList<Integer> selections) {
		String input = getInput();
		Scanner kb = new Scanner(input);

		while (kb.hasNext()) {
			selections.add(kb.nextInt());
		}

	}

	public String getInput() {
		Scanner kb = new Scanner(System.in);
		return kb.nextLine();
	}

	public static void main(String[] args) {

		quizschedule q = new quizschedule();
		Scanner kb = new Scanner(System.in);

		System.out.print("Enter course ID: ");
		String courseID = kb.next();

		q.doGet(courseID);
	}
}