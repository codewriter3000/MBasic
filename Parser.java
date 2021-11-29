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
            if(match(DO)) return function();
            if(matchPrim()){
                Token name = consume(IDENTIFIER, "Expect variable name.");
                Token type = previous();

                Expr initializer = null;
                if (match(EQUAL)) {
                    initializer = expression();
                }

                consume(SEMICOLON, "Expect ';' after variable declaration.");
                return new Stmt.Var(type, name, initializer);
                // if the next keyword is a varDecl, then make the variable assigned to a type
                // if it's a function, then make the function assigned to a type
            }
            if(match(NAMESPACE)) return namespace();
            return statement();
        } catch(ParseError error){
            synchronize();
        }
        return null;
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(null, name, initializer);
    }

    private Stmt statement(){
        if (match(IF)) return ifStatement();

        if (match(RETURN)) return returnStatement();

        if (match(LEFT_BRACE)) return new Stmt.Block(block());


        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt.Function function(){
        Token name = consume(IDENTIFIER, "Expect function name.");

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
        consume(LEFT_BRACE, "Expect '{' before function body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt namespace(){
        Token name = consume(IDENTIFIER, "Expect namespace name.");

        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Namespace(name, statements);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = or();

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or(){
        Expr expr = and();

        while(match(LOGICAL_OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and(){
        Expr expr = equality();

        while(match(LOGICAL_AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR, PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();

    }

    private Expr call() {
        Expr expr = primary();

        while (true) { // [while-true]
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);

            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {

                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }

                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);

        if (match(BIN, HEX, CHAR, INT, FLOAT, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");

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
