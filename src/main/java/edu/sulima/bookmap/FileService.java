package edu.sulima.bookmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileService {
    private final BufferedReader reader;

    private final BufferedWriter writer;

    private final String inputFilename;

    private final String outputFilename;



    public FileService(String inputFilename, String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
        try {
            reader = Files.newBufferedReader(Path.of(inputFilename));
            Path outputFilePath = Path.of(outputFilename);
            Files.deleteIfExists(outputFilePath);
            writer = Files.newBufferedWriter(outputFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLine(String line, boolean appendSpace) {
        try {
            writer.write(line);
            if (appendSpace) writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void close() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
