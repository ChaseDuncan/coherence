CP="./target/dependency/*:./target/classes/:./dist/*"
java -cp $CP org.junit.runner.JUnitCore edu.illinois.cs.cogcomp.locationcoherence.LocationCoherenceTest
