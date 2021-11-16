package com.micharksi.mbasic;

import java.util.ArrayList;
import java.util.List;

import static com.micharksi.mbasic.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {}


    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {

            statements.add(declaration());

        }

        return statements;
    }

    private Stmt declaration(){
        try {
            if(match(LET)) return varDeclaration();
            if(match(DO)) return function("function");
            if(matchPrim()){
                // if the next keyword is a varDecl, then make the variable assigned to a type
                // if it's a function, then make the function assigned to a type
            }
            if(match(NAMESPACE)) return namespace();
        } catch(ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt function(Token returnType){
        Token name = null;
        if(match(IDENTIFIER)){
            name = advance();
        }

        consume(LEFT_PAREN, "Expect '(' after function name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
    }

    private Stmt namespace(){
        Token name = consume(IDENTIFIER, "Expect namespace name.");

        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> functions = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            functions.add(function());
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Namespace(name, methods);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean matchPrim(){
        return match(BIN, CHAR, INT, STRING, HEX, FLOAT, BOOL);
    }


    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }


    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }


    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }


    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token next(){
        return tokens.get(current + 1);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    private ParseError error(Token token, String message) {
        MBasic.error(token, message);
        return new ParseError();
    }


    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case DO:
                case LET:
                case IF:
                case RETURN:
                case NAMESPACE:
                    return;
            }

            advance();
        }
    }
}
