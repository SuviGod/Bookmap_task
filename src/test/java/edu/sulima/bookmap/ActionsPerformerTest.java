package edu.sulima.bookmap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionsPerformerTest {

    private ActionsPerformer ap;


    public boolean equalsFile(String expected, String result) throws FileNotFoundException {
        Stream<String> gameStreamInput = new BufferedReader(
                new InputStreamReader(ClassLoader.getSystemResourceAsStream(expected))).lines();
        List<String> gameListExpected = gameStreamInput.collect(Collectors.toList());
        ClassLoader classLoader = ActionsPerformer.class.getClassLoader();
        Stream<String> gameStreamResult = new BufferedReader(new FileReader(result)).lines();
        List<String> gameListResult = gameStreamResult.toList();
        return gameListExpected.equals(gameListResult);
    }
    @Test
    public void overallSimpleTest() throws FileNotFoundException {
        final String input = "src/test/resources/simpleInput.txt";
        final String expectedOutput = "expectedSimpleOutput.txt";
        final String output = "output.txt";
        ap = new ActionsPerformer(input, output);
        ap.perform();

        Assertions.assertTrue(equalsFile(expectedOutput, output));

    }
}
