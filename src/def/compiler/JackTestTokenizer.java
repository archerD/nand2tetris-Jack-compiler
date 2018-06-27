package def.compiler;

import java.util.List;

/**
 * Created by dflor on 7/31/2017.
 */
class JackTestTokenizer extends JackTokenizer {

    Token[] tokens;
    int i;

    public JackTestTokenizer(Token[] tokens) {
        this.tokens = tokens;
        i = -1;
    }

    public JackTestTokenizer(List<Token> tokens) {
        this((Token[]) tokens.toArray());
    }

    @Override
    public boolean hasMoreTokens() {
        return i < tokens.length;
    }

    @Override
    public void advance() throws TokenizerError {
        i++;
    }

    @Override
    public TokenType tokenType() {
        return tokens[i].tokenType;
    }

    @Override
    public KeyWord keyWord() {
        return tokens[i].keyWord;
    }

    @Override
    public char symbol() {
        return tokens[i].symbol;
    }

    @Override
    public String identifier() {
        return tokens[i].identifier;
    }

    @Override
    public int intVal() {
        return tokens[i].intConstant;
    }

    @Override
    public String stringVal() {
        return tokens[i].stringConstant;
    }

    @Override
    public String comment() {
        return "";
    }

    @Override
    public void close() {
        return;
    }

    static class Token {
        public TokenType tokenType = null;

        public KeyWord keyWord = null;
        public char symbol = ' ';
        public String identifier = "";
        public int intConstant = 0;
        public String stringConstant = "";

        public Token(KeyWord keyWord) {
            this.keyWord = keyWord;
            tokenType = TokenType.KEYWORD;
        }

        public Token(char symbol) {
            this.symbol = symbol;
            tokenType = TokenType.SYMBOL;
        }

        public Token(int intConstant) {
            this.intConstant = intConstant;
        }

        public Token(TokenType tokenType, String identifierOrStringConstant) {
            this.tokenType = tokenType;
            if (tokenType == TokenType.IDENTIFIER)
                this.identifier = identifierOrStringConstant;
            else if (tokenType == TokenType.STRING_CONSTANT)
                this.stringConstant = identifierOrStringConstant;
            else
                throw new IllegalArgumentException("Token type must be IDENTIFIER or STRING_CONSTANT");
        }
    }
}
