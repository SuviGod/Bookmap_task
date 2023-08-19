package edu.sulima.bookmap;

import java.util.*;

public class ActionsPerformer {
    private final Queue<String> commands;

    private final FileService fileService;

    private Map<Integer, Integer> bidTable;

    private Map<Integer, Integer> askTable;

    public ActionsPerformer(String inputFilename, String outputFilename) {
        commands = new LinkedList<>();

        fileService = new FileService(inputFilename, outputFilename);

        initBidTable();
        initAskTable();

    }


    private void initBidTable() {
        bidTable = new HashMap<>();
    }
    private void initAskTable() {
        askTable = new HashMap<>();
    }

    public void perform() {
        commands.add(fileService.readLine());
        for (;!commands.isEmpty();) {
            Optional<String> maybeNextCommand =
                    Optional.ofNullable(fileService.readLine());
            maybeNextCommand.ifPresent(commands::add);

            String command = commands.poll();
            char actionType = command.charAt(0);

            switch (actionType) {
                case ('u') -> performUpdate(command);
                case ('q') -> performPrint(command);
                case ('o') -> performRemove(command);
                default -> throw new RuntimeException("Incorrect input format");
            }

        }
        fileService.close();

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
                if(size <=0) {
                    bidTable.remove(price);
                } else {
                    bidTable.put(price, size);
                }
            }
            case ("ask") -> {
                if(size <=0) {
                    askTable.remove(price);
                } else {
                    askTable.put(price, size);
                }
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
        boolean appendSpace = !commands.isEmpty();

        fileService.writeLine(Integer.toString(totalSize), appendSpace);

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
        StringBuilder sb = new StringBuilder()
                .append(maxPrice)
                .append(',')
                .append(size);
        boolean appendSpace = !commands.isEmpty();

        fileService.writeLine(sb.toString(), appendSpace);
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
        for (;sizeToRemove > 0 && !askTable.isEmpty();) {
            Integer price = askTable.keySet().stream()
                    .min(Integer::compareTo)
                    .get();

            sizeToRemove = removeProposal(sizeToRemove, price, askTable);
        }
    }

    private void removeBestBids(Integer sizeToRemove) {
        for (;sizeToRemove > 0 && !bidTable.isEmpty();) {
            Integer price = bidTable.keySet().stream()
                    .max(Integer::compareTo)
                    .get();

            sizeToRemove = removeProposal(sizeToRemove, price, bidTable);
        }

    }

    private Integer removeProposal(Integer sizeToRemove, Integer price, Map<Integer, Integer> bidTable) {
        Integer size = bidTable.get(price);
        if (size > sizeToRemove) {
            size -= sizeToRemove;
            bidTable.put(price, size);
            sizeToRemove = 0;
        } else {
            sizeToRemove -= size;
            bidTable.remove(price);
        }
        return sizeToRemove;
    }


}
