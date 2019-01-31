# Initial target servlet front end

classpath='../:javax.servlet-api.jar'

quizschedule : quizschedule.class
quizschedule.class : quizschedule.java servletUtils.class retakesReader.class retakeBean.class retakes.class quizReader.class quizzes.class quizBean.class apptsReader.class apptBean.class courseReader.class courseBean.class
	javac -cp $(classpath) quizschedule.java

servletUtils.class : servletUtils.java
	javac -cp $(classpath) servletUtils.java

quizReader.class : quizReader.java quizzes.class quizBean.java
	javac -cp $(classpath) quizReader.java

retakesReader.class : retakesReader.java retakes.class retakeBean.java
	javac -cp $(classpath) retakesReader.java

quizzes.class : quizzes.java quizBean.class
	javac -cp $(classpath) quizzes.java

quizBean.class : quizBean.java
	javac -cp $(classpath) quizBean.java

retakes.class : retakes.java retakeBean.class
	javac -cp $(classpath) retakes.java

retakeBean.class : retakeBean.java
	javac -cp $(classpath) retakeBean.java

apptsReader.class : apptsReader.java apptBean.class
	javac -cp $(classpath) apptsReader.java

apptBean.class : apptBean.java
	javac -cp $(classpath) apptBean.java

courseReader.class : courseReader.java courseBean.class
	javac -cp $(classpath) courseReader.java

courseBean.class : courseBean.java
	javac -cp $(classpath) courseBean.java

clean :
	rm *.class
