package com.micharksi.mbasic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.micharksi.mbasic.TokenType.*;

public class Tokenizer {
    private static final Map<String, TokenType> reservedWords;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        reservedWords = new HashMap<>();
        reservedWords.put("do",         DO);
        reservedWords.put("else",       ELSE);
        reservedWords.put("false",      FALSE);
        reservedWords.put("if",         IF);
        reservedWords.put("let",        LET);
        reservedWords.put("null",       NULL);
        reservedWords.put("return",     RETURN);
        reservedWords.put("true",       TRUE);
        reservedWords.put("namespace",  NAMESPACE);
        reservedWords.put("boolean",    BOOL);

        reservedWords.put("hex",        HEX);
        reservedWords.put("bin",        BIN);
        reservedWords.put("char",       CHAR);
        reservedWords.put("string",     STRING);
        reservedWords.put("int",        INT);
        reservedWords.put("float",      FLOAT);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    Tokenizer(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '%':
                addToken(PERCENT);
                break;

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '|':
                if(match('|')){
                    addToken(LOGICAL_OR);
                    break;
                }
            case '&':
                if(match('&')){
                    addToken(LOGICAL_AND);
                    break;
                }

            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case '0':
                if (match('x')) hex();
                if (match('b')) binary();

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;

            case '"':
                string();
                break;
            case '\'':
                character();
                break;

            case '$':
                advance();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    MBasic.error(line, "Unexpected character");
                }
                break;
            }
        }

        private void identifier(){
            while(isAlphaNumeric(peek())) advance();

            String text = source.substring(start, current);
            TokenType type = reservedWords.get(text);
            if(type == null) type = IDENTIFIER;
            addToken(type);
        }

        private void number(){
            boolean isFloat = false;
            while(isDigit(peek())) advance();

            if(peek() == '.' && isDigit(peekNext())){
                isFloat = true;
                advance();

                while(isDigit(peek())) advance();
            }

            if(isFloat)
                addToken(FLOAT, Double.parseDouble(source.substring(start, current)));
            else
                addToken(INT, Integer.parseInt(source.substring(start, current)));
        }

        private void binary(){
            while(isBinary(peek())) advance();

            addToken(BIN, source.substring(start, current));
        }

        private void hex(){
            while(isHex(peek())) advance();

            addToken(HEX, source.substring(start, current));
        }

        private void string(){
            while(peek() != '"' && !isAtEnd()){
                if(peek() == '\n') line++;
                advance();
            }

            if(isAtEnd()){
                MBasic.error(line, "String is not terminated.");
            }

            // The closing ".
            advance();

            // Trim the surrounding quotes.
            String value = source.substring(start + 1, current - 1);
            addToken(STRING, value);
        }

        private void character(){
        int characterCount = 0;
            while(peek() != '\'' && !isAtEnd()){
                if(++characterCount > 3 || (characterCount > 2 &&
                        peekPrevious() != '\\')){
                    MBasic.error(line, "Invalid syntax for a character.");
                }
                advance();
            }

            if(isAtEnd()){
                MBasic.error(line, "Character is not terminated.");
            }

            // The closing ".
            advance();

            // Trim the surrounding quotes.
            String value = source.substring(start + 1, current - 1);
            addToken(CHAR, value);
        }

    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }


    private char peek(){
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }


    private char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    } // [peek-next]

    private char peekPrevious(){
        if (current - 1 >= source.length()) return '\0';
        return source.charAt(current - 1);
    }


    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }


    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    } // [is-digit]


    private boolean isBinary(char c){
        return c == '1' || c == '0';
    }


    private boolean isHex(char c){
        return isDigit(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }


    private boolean isAtEnd() {
        return current >= source.length();
    }


    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
