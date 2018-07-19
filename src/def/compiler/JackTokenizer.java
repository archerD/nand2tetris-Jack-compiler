package def.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by dflor on 7/19/2017.
 * <p>
 * {@link JackTokenizer}: Removes all comments and white space from the input stream and
 * breaks it into Jack-language tokens, as specified by the Jack grammar.
 */
public class JackTokenizer {

    public static boolean NO_COMMENTS = true;

    private Scanner inputScanner;
    private String line;
    private String rawLine;
    private int lineNumber;

    private TokenType tokenType;
    private KeyWord keyWord;
    private char symbol;
    private String identifier;
    private int intVal;
    private String stringVal;
    private String comment;

    public static final String SYMBOLS = "{}()[].,;+-*/&|<>=~";
    public static final int SMALLEST_INT = 0;
    public static final int LARGEST_INT = 32767;
    public static final String DIGITS = "1234567890";
    public static final String VALID_IDENTIFIER_START_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" + "_";
    public static final String VALID_IDENTIFIER_CHARS = VALID_IDENTIFIER_START_CHARS + DIGITS;
    public static final String[] KEYWORDS = {"do", "if", "let", "var", "int", "char", "void", "true", "null", "this",
            "else", "class", "field", "false", "while", "method", "static",
            "return", "boolean", "function", "constructor"};
    public static final String INVALID_STRING_CHARACTERS = "\"\n";

    /**
     * Opens the input file and gets ready to tokenize it.
     *
     * @param jackInputFile the Jack file to tokenize
     */
    public JackTokenizer(File jackInputFile) throws FileNotFoundException {
        inputScanner = new Scanner(jackInputFile);
        lineNumber = 0;
        line = null;

        tokenType = null;
        keyWord = null;
        symbol = ' ';
        identifier = null;
        intVal = -1;
        stringVal = null;
        comment = null;
    }

    protected JackTokenizer() {

    }

    protected JackTokenizer(String testInput) {
        this(testInput, false);
    }

    protected JackTokenizer(String testInput, boolean appendExtraToken) {
        inputScanner = new Scanner(testInput + (appendExtraToken?" ;":""));
        lineNumber = 0;
        line = null;

        tokenType = null;
        keyWord = null;
        symbol = ' ';
        identifier = null;
        intVal = -1;
        stringVal = null;
        comment = null;
    }

    /**
     * Do we have more tokens in the output?
     *
     * @return true if there are more tokens, false if there are no more tokens
     */
    public boolean hasMoreTokens() {
        return inputScanner.hasNext() || (line != null && line.length() > 0);
    }

    /**
     * Gets the next token fom the input and makes it the current token.
     * This method should only be called if {@link #hasMoreTokens()} is true.
     * Initially there is no current token.
     */
    public void advance() throws TokenizerError {
        tokenType = null;
        keyWord = null;
        symbol = ' ';
        identifier = null;
        intVal = -1;
        stringVal = null;
        comment = null;

        // if the line is null or empty, get the next line
        while (line == null || line.trim().length() == 0) {
            getNextLine();
        }

        line = line.trim();
        char firstChar = line.charAt(0);

        // determine token type
        if (line.startsWith("//")) { // check for a line comment
            tokenType = TokenType.COMMENT;
        } else if (line.startsWith("/*")) { // check for a block comment
            tokenType = TokenType.BLOCK_COMMENT;
        } else if (SYMBOLS.indexOf(firstChar) != -1) { // if the character is in SYMBOL, the token is a symbol, we already ruled out comments
            tokenType = TokenType.SYMBOL;
        } else if (firstChar == '\"') {// the character is a ", so this is a string constant
            tokenType = TokenType.STRING_CONSTANT;
        } else if (DIGITS.indexOf(firstChar) != -1) { // starts with a digit, this is an integer
            tokenType = TokenType.INT_CONSTANT;
        } else if (VALID_IDENTIFIER_START_CHARS.indexOf(firstChar) != -1) { // this is an identifier or keyword
            tokenType = TokenType.IDENTIFIER; // assume it is an identifier, but could be a keyword
            for (String kw : KEYWORDS) { // loop over all keywords...
                if (line.startsWith(kw)) { // ...and check if the token is that keyword

                    // if the character after the possible keyword is a valid identifier, then we must keep looking
                    if (line.length() == kw.length() || VALID_IDENTIFIER_CHARS.indexOf(line.charAt(kw.length())) == -1) {
                        tokenType = TokenType.KEYWORD;
                        break;
                    }
                }
            }
        } else {
            throw new TokenizerError("Token not recognized!");
        }

        // parse token
        switch (tokenType) {
            case KEYWORD:
                for (String kw : KEYWORDS) { // loop over all keywords...
                    if (line.startsWith(kw)) { // ...and check if the token is that keyword
                        keyWord = KeyWord.getKeyWordFromString(kw);
                        line = line.substring(kw.length());
                    }
                }
                break;
            case SYMBOL:
                symbol = line.charAt(0);
                line = line.substring(1);
                break;
            case IDENTIFIER:
                StringBuilder idBuilder = new StringBuilder();
                int i;

                // we already know the first character is valid
                idBuilder.append(line.charAt(0));

                for (i = 1; i < line.length(); i++) {
                    // we can break either when we encounter a symbol or whitespace,
                    // or when we encounter an invalid identifier character

                    char currentChar = line.charAt(i);

                    // check for an end condition
                    if (Character.isWhitespace(currentChar) || SYMBOLS.indexOf(currentChar) != -1) {
                        break;
                    }

                    if (VALID_IDENTIFIER_CHARS.indexOf(currentChar) == -1) {
                        throw new TokenizerError("Invalid character \'" + currentChar + "\' encountered in identifier.");
                    }
                    idBuilder.append(currentChar);
                }

                identifier = idBuilder.toString();
                line = line.substring(i);

                break;
            case INT_CONSTANT:
                // TODO: expand to allow binary/octal/hexadecimal numbers?

                StringBuilder intBuilder = new StringBuilder();

                // we already know the first character is valid
                intBuilder.append(line.charAt(0));

                for (i = 1; i < line.length(); i++) {
                    // we can break either when we encounter a symbol or whitespace,
                    // or when we encounter a non digit character

                    char currentChar = line.charAt(i);

                    if (Character.isWhitespace(currentChar) || SYMBOLS.indexOf(currentChar) != -1) {
                        break;
                    }
                    if (DIGITS.indexOf(currentChar) == -1) {
                        throw new TokenizerError("Non Digit character \'" + currentChar + "\' encountered in integer constant.");
                    }

                    intBuilder.append(currentChar);
                }

                intVal = Integer.parseInt(intBuilder.toString());

                if (intVal < SMALLEST_INT || intVal > LARGEST_INT) {
                    throw new TokenizerError("Invalid integer constant \'" + intVal + "\'.  Valid range is " + SMALLEST_INT + " to " + LARGEST_INT + ".");
                }

                line = line.substring(i);
                break;
            case STRING_CONSTANT:
                int secondQuotation = line.indexOf('\"', 1);
                if (secondQuotation == -1) {
                    throw new TokenizerError("Invalid string constant, no closing \"");
                }

                stringVal = line.substring(1, secondQuotation);

                line = line.substring(secondQuotation+1);
                break;
            case COMMENT:
                comment = line.substring(2);
                line = null;
                if (NO_COMMENTS) {
                    advance();
                }
                break;
            case BLOCK_COMMENT:
                comment = "";
                line = line.substring(2);
                if (line.length()>0 && line.charAt(0) =='*') {
                    line = line.substring(1);
                }
                while (!line.contains("*/")) {
                    comment += line;
                    comment += "\n";
                    getNextLine();
                }

                comment += line.substring(0, line.indexOf("*/"));
                line = line.substring(line.indexOf("*/")+2);

                if (NO_COMMENTS) {
                    advance();
                }
                break;
        }
    }

    private void getNextLine() throws TokenizerError {
        if (!inputScanner.hasNextLine()) {
            throw new TokenizerError("Encountered end of file while tokenizing");
        }
        rawLine = inputScanner.nextLine();
        line = rawLine.trim();
        lineNumber++;
    }

    /**
     * Returns the type of the current token.
     *
     * @return an enum of type {@link TokenType}, representing the current token type.
     */
    public TokenType tokenType() {
        return tokenType;
    }

    /**
     * Returns the keyword which is the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#KEYWORD}.
     *
     * @return an enum of type {@link KeyWord}, representing the current keyword.
     */
    public KeyWord keyWord() {
        return keyWord;
    }

    /**
     * Returns the character which is the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#SYMBOL}.
     *
     * @return a char representing the current symbol
     */
    public char symbol() {
        return symbol;
    }

    /**
     * Returns the identifier which is the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#IDENTIFIER}
     *
     * @return a String that represents the current identifier.
     */
    public String identifier() {
        return identifier;
    }

    /**
     * Returns the integer value of the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#INT_CONSTANT}.
     *
     * @return a int that represents the current int constant.
     */
    public int intVal() {
        return intVal;
    }

    /**
     * Returns the string value of the current token, without the double quotes.
     * Should be called only when {@link #tokenType()} is {@link TokenType#STRING_CONSTANT}.
     *
     * @return a string representing the string constant.
     */
    public String stringVal() {
        return stringVal;
    }

    public String comment() {
        return comment;
    }

    /**
     * To represent the 5 different token types,
     * plus comments and block comments.
     */
    enum TokenType {
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONSTANT,
        STRING_CONSTANT,
        COMMENT,
        BLOCK_COMMENT;
    }

    enum KeyWord {
        CLASS,
        METHOD,
        FUNCTION,
        CONSTRUCTOR,
        INT,
        BOOLEAN,
        CHAR,
        VOID,
        VAR,
        STATIC,
        FIELD,
        LET,
        DO,
        IF,
        ELSE,
        WHILE,
        RETURN,
        TRUE,
        FALSE,
        NULL,
        THIS;

        public static KeyWord getKeyWordFromString(String string) {
            switch (string) {
                case "class":
                    return CLASS;
                case "method":
                    return METHOD;
                case "function":
                    return FUNCTION;
                case "constructor":
                    return CONSTRUCTOR;
                case "int":
                    return INT;
                case "boolean":
                    return BOOLEAN;
                case "char":
                    return CHAR;
                case "void":
                    return VOID;
                case "var":
                    return VAR;
                case "static":
                    return STATIC;
                case "field":
                    return FIELD;
                case "let":
                    return LET;
                case "do":
                    return DO;
                case "if":
                    return IF;
                case "else":
                    return ELSE;
                case "while":
                    return WHILE;
                case "return":
                    return RETURN;
                case "true":
                    return TRUE;
                case "false":
                    return FALSE;
                case "null":
                    return NULL;
                case "this":
                    return THIS;
                default:
                    throw new IllegalArgumentException("string \'" + string + "\' is not a valid keyword.");
            }
        }
    }

    public void close() {
        inputScanner.close();
    }

    public class TokenizerError extends RuntimeException {
        public String getErrorLine() {
            return errorLine;
        }

        public int getErrorLineNumber() {
            return errorLineNumber;
        }

        public String getUntokenizedLinePortion() {
            return untokenizedLinePortion;
        }

        public TokenType getAssumedTokenType() {
            return assumedTokenType;
        }

        public TokenizerError(String error) {
            super(error);
            errorLine = rawLine;
            errorLineNumber = lineNumber;
            untokenizedLinePortion = line;
            assumedTokenType = tokenType;

        }

        private String errorLine;
        private int errorLineNumber;
        private String untokenizedLinePortion;
        private TokenType assumedTokenType;
    }
}
