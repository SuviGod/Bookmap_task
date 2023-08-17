package edu.sulima.bookmap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class ActionsPerformer {
    private final List<String> commands;

    private final BufferedWriter writer;

    private PriorityQueue<Integer[]> bidTable;

    private PriorityQueue<Integer[]> askTable;

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
        bidTable = new PriorityQueue<>((raw1, raw2) -> {
            if(raw1[1] > 0) {
                if(raw2[1] > 0) {
                    return -1 * raw1[0].compareTo(raw2[0]);
                }
                return raw1[0];
            }
            return raw1[0];
        });
    }
    private void initAskTable() {
        askTable = new PriorityQueue<>((raw1, raw2) -> {
            if(raw1[1] > 0) {
                if(raw2[1] > 0) {
                    return raw1[0].compareTo(raw2[0]);
                }
                return raw1[0];
            }
            return raw1[0];
        });
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
                Integer [] rawToInsert = findBidByPrice(price)
                        .orElseGet(() -> new Integer[2]);
                bidTable.remove(rawToInsert);
                rawToInsert[0] = price;
                rawToInsert[1] = size;
                bidTable.add(rawToInsert);
            }
            case ("ask") -> {
                Integer [] rawToInsert = findAskByPrice(price)
                        .orElseGet(() -> new Integer[2]);
                askTable.remove(rawToInsert);
                rawToInsert[0] = price;
                rawToInsert[1] = size;
                askTable.add(rawToInsert);
            }
            default -> throw new RuntimeException("incorrect input format");
        }
    }

    private Optional<Integer []> findBidByPrice(Integer price) {
        return bidTable.stream()
                .filter( raw -> raw[0].equals(price))
                .findFirst();
    }

    private Optional<Integer []> findAskByPrice(Integer price) {
        return askTable.stream()
                .filter( raw -> raw[0].equals(price))
                .findFirst();
    }

    private void performPrint(String command) {
        command = command.substring(2);
        switch (command) {
            case ("best_bid") -> printBestProposalFromTable(bidTable);
            case ("best_ask") -> printBestProposalFromTable(askTable);
            default -> printSizeAtPrice(command);
        }
    }

    private void printSizeAtPrice(String command) {
        List<String> commandParts = List.of(command.split(","));

        if(commandParts.size() != 2 || !commandParts.get(0).equals("size")) {
            throw new RuntimeException("Incorrect input format");
        }

        Integer price = Integer.parseInt(commandParts.get(1));

        Integer askSize = askTable.stream()
                .filter(raw -> price.equals(raw[0]))
                .findFirst()
                .orElseGet(() -> {
                    Integer [] emptyRaw = new Integer[2];
                    emptyRaw[1] = 0;
                    return emptyRaw;
                })[1];

        Integer bidSize = bidTable.stream()
                .filter(raw -> price.equals(raw[0]))
                .findFirst()
                .orElseGet(() -> {
                    Integer [] emptyRaw = new Integer[2];
                    emptyRaw[1] = 0;
                    return emptyRaw;
                })[1];
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

    private void printBestProposalFromTable(PriorityQueue<Integer []> table) {
        Integer [] priceAndSize = table.peek();

        try {
            writer.write(priceAndSize[0].toString());
            writer.write(',');
            writer.write(priceAndSize[1].toString());
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

    private void removeBestAsks(Integer size) {
        removeBestsFromTable(size, askTable);
    }

    private void removeBestBids(Integer size) {
        removeBestsFromTable(size, bidTable);
    }
    private void removeBestsFromTable(Integer size, PriorityQueue<Integer []> table) {
        for (;size > 0;) {
            Integer [] raw = table.peek();
            if (raw[1] > size) {
                raw[1] -= size;
                size = 0;
            } else {
                size -= raw[1];
                table.remove(raw);
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
