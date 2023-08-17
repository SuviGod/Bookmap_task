package edu.sulima.bookmap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ActionsPerformer {
    private final List<String> commands;

    private final BufferedWriter writer;

    private Map<Integer, Integer> bidTable;

    private Map<Integer, Integer> askTable;

    private int currentCommandNumber = 0;

    private int lastPrintOperationNumber = 0;

    public ActionsPerformer(String inputFilename, String outputFilename) {
        commands = readFile(inputFilename);

        prepareFile(outputFilename);

        writer = getWriter(outputFilename);

        initBidTable();
        initAskTable();

        lastPrintOperationNumber = getLastPrintOperationNumber();
    }

    private int getLastPrintOperationNumber() {
        int last = 0;
        for(int i = 0; i < commands.size(); i++) {
            if(commands.get(i).charAt(0) == 'q') {
                last = i;
            }
        }
        return last + 1;
    }

    private void prepareFile(String outputFilename) {
        Path outputFilePath = Path.of(outputFilename);
        try {
            Files.deleteIfExists(outputFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initBidTable() {
        bidTable = new HashMap<>();
    }
    private void initAskTable() {
        askTable = new HashMap<>();
    }

    public void perform() {
        for (String command: commands) {
            currentCommandNumber++;
            char actionType = command.charAt(0);

            switch (actionType) {
                case ('u') -> performUpdate(command);
                case ('q') -> performPrint(command);
                case ('o') -> performRemove(command);
                default -> throw new RuntimeException("Incorrect input format");
            }
        }
        closeWriter();

    }

    private void closeWriter() {
        try {

            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void performUpdate(String command) {
        command = command.substring(2);
        List<String> commandParts = List.of(command.split(","));

        if(commandParts.size() != 3)
            throw new RuntimeException("Incorrect input format");

        Integer price = Integer.parseInt(commandParts.get(0));
        int size = Integer.parseInt(commandParts.get(1));

        switch (commandParts.get(2)) {
            case ("bid") -> {
                bidTable.put(price, size);
            }
            case ("ask") -> {
                askTable.put(price, size);
            }
            default -> throw new RuntimeException("incorrect input format");
        }
    }

    private Optional<Integer> findBidByPrice(Integer price) {
        return Optional.ofNullable(
                bidTable.getOrDefault(price, null));
    }

    private Optional<Integer> findAskByPrice(Integer price) {
        return Optional.ofNullable(
                askTable.getOrDefault(price, null));
    }

    private void performPrint(String command) {
        command = command.substring(2);
        switch (command) {
            case ("best_bid") -> printBestBid();
            case ("best_ask") -> printBestAsk();
            default -> printSizeAtPrice(command);
        }
    }

    private void printSizeAtPrice(String command) {
        List<String> commandParts = List.of(command.split(","));

        if(commandParts.size() != 2 || !commandParts.get(0).equals("size")) {
            throw new RuntimeException("Incorrect input format");
        }

        Integer price = Integer.parseInt(commandParts.get(1));

        Integer askSize = findAskByPrice(price).orElse(0);

        Integer bidSize = findBidByPrice(price).orElse(0);
        int totalSize = bidSize + askSize;
        try {
            writer.write(Integer.toString(totalSize));
            if(currentCommandNumber != lastPrintOperationNumber) {
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void printBestAsk() {
        Integer maxPrice = Collections.min(askTable.keySet());
        printBestProposal(maxPrice, askTable);
    }

    private void printBestBid() {
        Integer maxPrice = Collections.max(bidTable.keySet());
        printBestProposal(maxPrice, bidTable);
    }

    private void printBestProposal(Integer maxPrice, Map<Integer, Integer> bidTable) {
        Integer size = bidTable.get(maxPrice);
        try {
            writer.write(maxPrice.toString());
            writer.write(',');
            writer.write(size.toString());
            if(currentCommandNumber != lastPrintOperationNumber) {
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void performRemove(String command) {
        command = command.substring(2);

        List<String> commandParts = List.of(command.split(","));

        if(commandParts.size() != 2)
            throw new RuntimeException("Incorrect input format");

        String actionType = commandParts.get(0);
        Integer size = Integer.parseInt(commandParts.get(1));
        switch (actionType) {
            case ("buy") -> removeBestAsks(size);
            case ("sell") -> removeBestBids(size);
            default -> throw new RuntimeException("Incorrect input format");
        }
    }

    private void removeBestAsks(Integer sizeToRemove) {
        List<Integer> sortedPrices = askTable
                .keySet()
                .stream()
                .sorted()
                .toList();
        removeBestProposals(sizeToRemove, sortedPrices, askTable);

    }

    private void removeBestBids(Integer sizeToRemove) {
        List<Integer> sortedPrices = bidTable
                .keySet()
                .stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        removeBestProposals(sizeToRemove, sortedPrices, bidTable);

    }

    private void removeBestProposals(Integer sizeToRemove, List<Integer> sortedPrices, Map<Integer, Integer> table) {
        for (int i = 0;sizeToRemove > 0 && i < sortedPrices.size();i++) {
            Integer price = sortedPrices.get(i);
            Integer size = table.get(price);
            if (size > sizeToRemove) {
                size -= sizeToRemove;
                table.put(price, size);
                sizeToRemove = 0;
            } else {
                sizeToRemove -= size;
                table.remove(price);
            }
        }
    }


    private BufferedWriter getWriter(String outputFilename) {
        try {
            return new BufferedWriter(new FileWriter(outputFilename, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> readFile(String inputFilename) {

        Path inputFilePath = Path.of(inputFilename);

        List<String> commands;
        try {
            commands = Files.readAllLines(inputFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return commands;
    }
}
