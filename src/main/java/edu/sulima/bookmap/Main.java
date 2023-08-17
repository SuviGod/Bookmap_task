package edu.sulima.bookmap;

public class Main {
    public static void main(String[] args) {

        ActionsPerformer actionsPerformer = new ActionsPerformer(
                "input.txt",
                "output.txt");
        actionsPerformer.perform();
    }
}