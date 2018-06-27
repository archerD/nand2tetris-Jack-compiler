package def.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by dflor on 8/8/2017.
 *
 * Emits VM commands into a file, using the VM command syntax.
 */
public class VMWriter {

    private PrintWriter writer;

    /**
     * Creates a new file and prepares it for writing.
     * @param outputFile The file to output to.
     */
    public VMWriter(File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);
    }

    public VMWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * Writes a VM push command.
     * @param segment The segment the push command is to.
     * @param index The index for the push command.
     */
    public void writePush(Segment segment, int index) {
        writer.println("    push " + segment.toString().toLowerCase() + " " + index);
    }

    /**
     * Writes a VM pop command.
     * @param segment The segment for the pop command.
     * @param index The index for the pop command.
     */
    public void writePop(Segment segment, int index) {
        writer.println("    pop " + segment.toString().toLowerCase() + " " + index);
    }

    /**
     * Writes a VM arithmetic command.
     * @param command Which command is being written.
     */
    public void writeArithmetic(Command command) {
        writer.println("    " + command.toString().toLowerCase());
    }

    /**
     * Writes a VM label command.
     * @param label The name for the label.
     */
    public void writeLabel(String label) {
        writer.println("label " + label);
    }

    /**
     * Writes a VM goto command.
     * @param label the name of the label to jump to.
     */
    public void writeGoto(String label) {
        writer.println("    goto " + label);
    }

    /**
     * Writes a VM if-goto command.
     * @param label the name of the label to jump to.
     */
    public void writeIf(String label) {
        writer.println("    if-goto " + label);
    }

    /**
     * Writes a VM call command.
     * @param name The name of the function to call.
     * @param nArgs The number of arguments pushed onto the stack
     */
    public void writeCall(String name, int nArgs) {
        writer.println("    call " + name + " " + nArgs);
    }

    /**
     * Writes a VM function command.
     * @param name The name of the function being declared.
     * @param nLocals The number of local variables for this function.
     */
    public void writeFunction(String name, int nLocals) {
        writer.println("function " + name + " " + nLocals);
    }

    /**
     * Writes a VM return command.
     */
    public void writeReturn() {
        writer.println("    return");
    }

    /**
     * Closes the output file.
     */
    public void close() {
        writer.flush();
        writer.close();
    }

    public enum Segment {
        CONSTANT,
        ARGUMENT,
        LOCAL,
        STATIC,
        THIS,
        THAT,
        POINTER,
        TEMP;
    }

    public enum Command {
        ADD,
        SUB,
        NEG,
        EQ,
        GT,
        LT,
        AND,
        OR,
        NOT;

        public static Command getBinaryCommandFromSymbol(char symbol) {
            Command rtn = null;
            switch (symbol) {
                case '+':
                    rtn = ADD;
                    break;
                case '-':
                    rtn = SUB;
                    break;
                case '=':
                    rtn = EQ;
                    break;
                case '>':
                    rtn = GT;
                    break;
                case '<':
                    rtn = LT;
                    break;
                case '&':
                    rtn = AND;
                    break;
                case '|':
                    rtn = OR;
                    break;
            }

            return rtn;
        }

        public static Command getUnaryCommandFromSymbol(char symbol) {
            Command rtn = null;
            switch (symbol) {
                case '-':
                    rtn = NEG;
                    break;
                case '~':
                    rtn = NOT;
                    break;
            }
            return rtn;
        }
    }
}
