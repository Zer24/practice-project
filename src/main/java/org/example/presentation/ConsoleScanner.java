package org.example.presentation;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ConsoleScanner {

    private final Scanner scanner;

    public ConsoleScanner() {
        this.scanner = new Scanner(System.in);
    }

    public Scanner getScanner() {
        return scanner;
    }

    public String nextLine() {
        return scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}