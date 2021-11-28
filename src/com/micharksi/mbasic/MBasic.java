package com.micharksi.mbasic;

import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class MBasic {

    // private static final Interpreter interpreter = new Interpreter();
    static boolean compileError = false;
    static boolean runtimeError = false;

    public static void main(String[] args){
        switch(args.length){
            case 1:
                runPath(args[0]);
                break;
            case 0:
                runPrompt();
                break;
            default:
                System.out.println("Expected only 1 argument, if any");
        }
    }

    public static void runPath(String path){
        //TODO
    }

    public static void run(String source){
        Tokenizer tokenizer = new Tokenizer(source);
        List<Token> tokens = tokenizer.scanTokens();
        System.out.println(tokens);

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if(compileError) error();

//        Resolver resolver = new Resolver(interpreter);
//        resolver.resolve();
//
//        if(compileError) error();
//
//        interpreter.interpret(statements);

        if(compileError) error();
    }

    public static void runPrompt(){
        Scanner scan = new Scanner(System.in);
        StringBuilder source = new StringBuilder();
        do {
            System.out.print("MBasic>> ");
            String line = scan.nextLine();
            if(line.equals("exit")){
                System.out.println("Now exiting MBasic. Have a great day :)");
                exit(0);
            } else if(line.endsWith("$")){
                source.append(line);
                source.deleteCharAt(source.length()-1);
                run(source.toString());
                source = new StringBuilder();
            } else {
                source.append(line);
            }
        } while(true);
    }

    public static void error(){
        System.out.println("ERROR$>> An error has occurred.");
        System.exit(1);
    }

    public static void error(int line, String message){
        System.out.println("Line #" + line + ">> " + message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        compileError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        runtimeError = true;
    }
}
