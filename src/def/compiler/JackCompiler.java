package def.compiler;

import def.compiler.JackTokenizer.TokenType;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by dflor on 7/19/2017.
 * <p>
 * The analyzer program operates on a given source, where source is either a file name
 * of the form Xxx.jack or a directory name containing one or more such files. For
 * each source Xxx.jack file, the analyzer goes through the following logic:
 * 1.  Create a {@link JackTokenizer} from the Xxx.jack input file.
 * 2.  Create an output file called Xxx.xml and prepare it for writing.
 * 3.  Use the {@link CompilationEngine} to compile the input {@link JackTokenizer} into the output file.
 *
 * V2
 * The compiler operates on a given source where source is either a file name of the
 * form Xxx.jack or a directory name containing one or more such files.  For each
 * Xxx.jack input file, the compiler creates a JackTokenizer and an output Xxx.vm file.
 * Next, the compiler uses the CompilationEngine, SymbolTable, and VMWriter modules
 * to write the output file.
 */
public class JackCompiler {
    // ALGORITHM:
    // get input file name
    // create output file name and stream

    // print out "done" message to user
    // close output file stream
    public static void main(String[] args) {

        String inputFileName = null, outputFileName = null;
        File inputFile = null;
        File[] compilerFiles = null, outputFiles = null;

        Mode mode = Mode.COMPILE;

        //get input file name from command line or console input
        if (args.length > 0) {
            System.out.println("command line directory/file = " + args[args.length - 1]);
            //inputFileName = args[args.length - 1];

            if (args[0].charAt(0) == '-') {
                String tag = args[0];

                if (tag.equalsIgnoreCase("-t") || tag.equalsIgnoreCase("--tokenize")) {
                    mode = Mode.TOKENIZE;
                } else if (tag.equalsIgnoreCase("-p") || tag.equalsIgnoreCase("--parse")) {
                    mode = Mode.PARSE;
                } else if (tag.equalsIgnoreCase("-e") || tag.equalsIgnoreCase("--extended-parse")) {
                    mode = Mode.EXTENDED_PARSE;
                }else if (tag.equalsIgnoreCase("-c") || tag.equalsIgnoreCase("--compile")) {
                    mode = Mode.COMPILE;
                } else {
                    System.out.println("invalid tag " + tag);
                    System.out.println("valid tags are -t, -c, -e, -p, --tokenize, --parse, --extended-parse, and --compile");
                    System.exit(0);
                }

                System.out.println("mode is " + mode);
            }
        }
        if (inputFileName == null){
            Scanner keyboard = new Scanner(System.in);

            System.out.println("Please enter Jack file/directory name you would like to compile.");
            System.out.println("Don't forget the .jack extension, if a file: ");
            inputFileName = keyboard.nextLine();

            keyboard.close();
        }

        // make the input a File
        inputFile = new File(inputFileName);

        if (!inputFile.exists()) { //handle the case where the input file does not exist
            System.out.println("The specified file/directory, " + inputFile + ", does not exist");
            System.exit(0);
        }

        //switch between if the inputFile is a directory or not
        if (inputFile.isDirectory()) {

            //File::isFile as parameter instead?
            compilerFiles = inputFile.listFiles(new FileFilter() { // if it is a directory, get an array of the files in the directory that are jack files
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".jack");
                }
            });

            //handle an empty/nonexistent list
            if (compilerFiles == null || compilerFiles.length == 0) {
                System.out.println("No Jack files in directory " + inputFile);
                System.exit(0);
            } else {
                outputFiles = new File[compilerFiles.length];

                for (int i = 0; i < compilerFiles.length; i++) {
                    outputFileName = compilerFiles[i].getName();
                    outputFileName = outputFileName.substring(0, outputFileName.length() - 4);

                    outputFiles[i] = new File(inputFile, outputFileName + mode.fileSuffix());
                }
            }
        } else {
            outputFileName = inputFileName.substring(0, inputFileName.length() - 4) + mode.fileSuffix();
            outputFiles = new File[1];
            outputFiles[0] = new File(outputFileName); // create output file name
            compilerFiles = new File[1]; // create the compiler file array
            compilerFiles[0] = inputFile;
        }


        for (int i = 0; i < compilerFiles.length; i++) {
            JackTokenizer tokenizer = null;
            try {
                tokenizer = new JackTokenizer(compilerFiles[i]); // open new tokenizer for an import file
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Could not open file " + compilerFiles[i]);
                System.out.println("Run program again, make sure you have read permissions, etc.");
                System.exit(0);
            }

            CompilationEngine compilationEngine = null;
            try {
                if (mode != Mode.TOKENIZE) {
                    compilationEngine = new CompilationEngine(tokenizer, outputFiles[i]);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Could not open file " + outputFiles[i]);
                System.out.println("Run program again, make sure you have write permissions, etc.");
                System.exit(0);
            }
            PrintWriter writer = null;

            // begin compilation
            try {
            switch (mode) {
                case TOKENIZE:
                    try {
                        writer = new PrintWriter(outputFiles[i]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("Could not open file " + outputFiles[i]);
                        System.out.println("Run program again, make sure you have write permissions, etc.");
                        System.exit(0);
                    }

                    writer.println("<tokens>");

                    while (tokenizer.hasMoreTokens()) {
                        tokenizer.advance();

                        TokenType tokenType = tokenizer.tokenType();

                        switch (tokenType) {
                            case KEYWORD:
                                writer.println("<keyword> " + tokenizer.keyWord().toString().toLowerCase() + " </keyword>");
                                break;
                            case SYMBOL:
                                char symbol = tokenizer.symbol();

                                if (symbol == '<') {
                                    writer.println("<symbol> &lt; </symbol>");
                                } else if (symbol == '>') {
                                    writer.println("<symbol> &gt; </symbol>");
                                } else if (symbol == '&') {
                                    writer.println("<symbol> &amp; </symbol>");
                                } else {
                                    writer.println("<symbol> " + symbol + " </symbol>");
                                }
                                break;
                            case IDENTIFIER:
                                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                                break;
                            case INT_CONSTANT:
                                writer.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
                                break;
                            case STRING_CONSTANT:
                                writer.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
                                break;
                            case COMMENT:
                                System.out.println("comment(//):" + tokenizer.comment());
                                break;
                            case BLOCK_COMMENT:
                                System.out.println("comment(/*):" + tokenizer.comment());
                                break;
                        }
                    }

                    writer.println("</tokens>");
                    writer.close();
                    break;
                case EXTENDED_PARSE:
                    CompilationEngine.extendedXML = true;
                case PARSE:
                    CompilationEngine.outputXML = true;
                    compilationEngine.compileClass();
                    compilationEngine.close();
                    break;
                case COMPILE:
                    CompilationEngine.outputXML = false;
                    compilationEngine.compileClass();
                    compilationEngine.close();
                    break;
            }


            } catch (JackTokenizer.TokenizerError e) {
                System.out.println(e.getMessage()); //print the error message
                System.out.println("Error at line " + e.getErrorLine() + ": " + e.getErrorLine()); //add debugging data
                System.out.println("Line Tokenized to: " + e.getUntokenizedLinePortion());
                System.out.println("Assumed Token type: " + e.getAssumedTokenType());
                System.exit(1); //exit program
            } catch (CompilationEngine.CompilationException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                e.closeCompilationEngine();
                System.exit(1);
            }
        }

        //alert user and close output stream
        System.out.println("Done!");
    }

    enum Mode {
        TOKENIZE,
        PARSE,
        EXTENDED_PARSE,
        COMPILE;

        public String fileSuffix() {
            switch (this) {

                case TOKENIZE:
                    return "xml";
                case PARSE:
                    return "xml";
                case EXTENDED_PARSE:
                    return "xml";
                case COMPILE:
                    return "vm";
            }
            return null;
        }
    }
}
