package def.compiler;

import java.util.HashMap;

/**
 * Created by dflor on 8/8/2017.
 *
 * This module provides services for creating and using a symbol table.  Recall that each
 * symbol has a scope from which it is visible in the source code.  The symbol table
 * implements this abstraction by giving each symbol a running number (index) within
 * the scope.  The index starts at 0, increments by 1 each time an identifier is added to
 * the table, and resets to 0 when starting a new scope.  The following kinds of identifiers
 * may appear in the symbol table:
 *  Static:  Scope: class.
 *  Field:  Scope: class.
 *  Argument:  Scope: subroutine (method/function/constructor).
 *  Var:  Scope: subroutine (method/function/constructor).
 * When compiling error-free Jack code, any identifier not found in the symbol table
 * may be assumed to be a subroutine name or a class name.  Since the Jack language
 * syntax rules suffice for distinguishing between these two possibilities, and since no
 * "linking" needs to be done by the compiler, there is no need to keep these identifiers
 * in the symbol table.
 *
 * {@link SymbolTable} Provides a symbol table abstraction.  The Symbol table associates the
 * identifier names found in the program with identifier properties needed for compilation:
 * type, kind, and running index.  The symbol table for Jack programs has two nested scopes (class/subroutine).
 */
public class SymbolTable {
    private HashMap<String, indexTypeAndKind> classScope;
    private HashMap<String, indexTypeAndKind> subroutineScope;

    private int staticIndex;
    private int fieldIndex;
    private int varIndex;
    private int argIndex;

    /**
     * Creates a new empty symbol table.
     */
    public SymbolTable() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();

        staticIndex = 0;
        fieldIndex = 0;
        varIndex = 0;
        argIndex = 0;
    }

    /**
     * Starts a new subroutine scope (i.e., resets the subroutine's symbol table).
     */
    public void startSubroutine() {
        subroutineScope.clear();

        varIndex = 0;
        argIndex = 0;
    }

    /**
     * Defines a new identifier of a given name, type, and kind and assigns it a running index.
     * STATIC and FIELD identifiers have a class scope, while ARG and VAR identifiers have a subroutine scope.
     * @param name The name for the symbol
     * @param type The type of the symbol, either a primitive or a class
     * @param kind The kind of the symbol, STATIC, ARG, VAR, or FIELD.
     */
    public void define(String name, String type, Kind kind) {
        if (kind.scopeIsClass() && classScope.containsKey(name))
            throw new SymbolExistsException(name, type, kind);
        else if (kind.scopeIsSubroutine() && subroutineScope.containsKey(name))
            throw new SymbolExistsException(name, type, kind);
        else if (kind == Kind.NONE)
            throw new IllegalArgumentException("The kind " + Kind.NONE + " is not a valid kind");

        switch (kind) {
            case STATIC:
                classScope.put(name, new indexTypeAndKind(staticIndex, type, kind));
                staticIndex++;
                break;
            case FIELD:
                classScope.put(name, new indexTypeAndKind(fieldIndex, type, kind));
                fieldIndex++;
                break;
            case ARG:
                subroutineScope.put(name, new indexTypeAndKind(argIndex, type, kind));
                argIndex++;
                break;
            case VAR:
                subroutineScope.put(name, new indexTypeAndKind(varIndex, type, kind));
                varIndex++;
                break;
        }
    }

    /**
     * Returns the number of variables of the given kind already defined in the current scope.
     * @param kind The kind of symbol to get a count of
     * @return The number of variables of the given kind
     */
    public int varCount(Kind kind) {
        int count = 0;

        if (kind == null || kind == Kind.NONE)
            throw new IllegalArgumentException("not a valid type!");

        switch (kind) {
            case STATIC:
                count = staticIndex;
                break;
            case FIELD:
                count = fieldIndex;
                break;
            case ARG:
                count = argIndex;
                break;
            case VAR:
                count = varIndex;
                break;
        }

        return count;
    }

    /**
     * Returns the kind of the named identifier in the current scope.
     * If the identifier is unknown in the current scope, returns NONE.
     * @param name The name of the symbol to check the kind.
     * @return The kind of the symbol with name name.
     */
    public Kind kindOf(String name) {
        indexTypeAndKind kind = getSymbolData(name);
        return kind!=null?kind.kind: Kind.NONE;
    }

    /**
     * Returns the type of the named identifier in the current scope.
     * @param name The name of the symbol to get the type of
     * @return The type of the symbol, either a primitive or a class
     */
    public String typeOf(String name) {
        indexTypeAndKind type = getSymbolData(name);
        return type!=null?type.type:null;
    }

    /**
     * Returns the index assigned to the named identifier.
     * @param name The name of the symbol to get the index of.
     * @return The index assigned to the symbol with name name.
     */
    public int indexOf(String name) {
        indexTypeAndKind index = getSymbolData(name);
        return index!=null?index.index:-1;
    }

    private indexTypeAndKind getSymbolData(String name) {
        indexTypeAndKind data;

        data = subroutineScope.get(name);

        if (data == null) {
            data = classScope.get(name);
        }

        return data;
    }

    public enum Kind {
        STATIC,
        FIELD,
        ARG,
        VAR,
        NONE;

        public boolean scopeIsSubroutine() {
            return this == ARG || this == VAR;
        }

        public boolean scopeIsClass() {
            return this == STATIC || this == FIELD;
        }
    }

    public class SymbolExistsException extends RuntimeException {
        public SymbolExistsException(String duplicateName, String duplicateType, Kind duplicateKind) {
            super("The symbol \'" + duplicateName + "\' already exists in the " + (duplicateKind.scopeIsClass()?"class":"subroutine") + " scope!");
        }
    }

    private static class indexTypeAndKind {
        int index;
        String type;
        Kind kind;

        public indexTypeAndKind(int index, String type, Kind kind) {
            this.index = index;
            this.type = type;
            this.kind = kind;
        }
    }
}
