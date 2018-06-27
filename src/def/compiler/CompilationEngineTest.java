package def.compiler;

import org.junit.Test;

import java.io.File;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dflor on 7/30/2017.
 */
public class CompilationEngineTest {
    @Test
    public void compileClassVariableDeclaration() throws Exception {
        JackTokenizer tokenizer;
        tokenizer = new JackTokenizer("static boolean test; field int x, y; field Square square;", true);

        File outputFile = new File("temp.xml");
        CompilationEngine compilationEngine = new CompilationEngine(tokenizer, outputFile);

        compilationEngine.compileClassVariableDeclaration();
        compilationEngine.compileClassVariableDeclaration();
        compilationEngine.compileClassVariableDeclaration();

        compilationEngine.close();

        Scanner compilationEngineOutput = new Scanner(outputFile);

        String[] expectedOutputLines = {"<classVarDec>", "  <keyword> static </keyword>", "  <keyword> boolean </keyword>",
                "  <identifier> test </identifier>", "  <symbol> ; </symbol>", "</classVarDec>",
                "<classVarDec>", "  <keyword> field </keyword>", "  <keyword> int </keyword>",
                "  <identifier> x </identifier>", "  <symbol> , </symbol>", "  <identifier> y </identifier>",
                "  <symbol> ; </symbol>", "</classVarDec>",
                "<classVarDec>", "  <keyword> field </keyword>", "  <identifier> Square </identifier>",
                "  <identifier> square </identifier>", "  <symbol> ; </symbol>", "</classVarDec>"};

        for (int i = 0; i < expectedOutputLines.length; i++) {

            assertTrue("insufficient lines in output", compilationEngineOutput.hasNextLine());

            String actualOutputLine = compilationEngineOutput.nextLine();

            assertEquals("Line comparision failure at line " + i, expectedOutputLines[i], actualOutputLine);
        }

        assertTrue("Extra lines in output", !compilationEngineOutput.hasNextLine());

        compilationEngineOutput.close();
        outputFile.delete();

    }

    @Test
    public void compileParameterList() throws Exception {
        JackTokenizer tokenizer = new JackTokenizer(") int Ax, int Ay, int Asize)");
        File outputFile = new File("temp.xml");
        CompilationEngine compilationEngine = new CompilationEngine(tokenizer, outputFile);

        compilationEngine.compileParameterList();
        assertTrue(tokenizer.symbol() == ')');
        tokenizer.advance();
        compilationEngine.compileParameterList();
        assertTrue(tokenizer.symbol() == ')');

        compilationEngine.close();

        Scanner compilationEngineOutput = new Scanner(outputFile);

        String[] expectedOutputLines = {"<parameterList>", "</parameterList>",
                "<parameterList>", "  <keyword> int </keyword>", "  <identifier> Ax </identifier>",
                "  <symbol> , </symbol>", "  <keyword> int </keyword>", "  <identifier> Ay </identifier>",
                "  <symbol> , </symbol>", "  <keyword> int </keyword>", "  <identifier> Asize </identifier>",
                "</parameterList>"};

        for (int i = 0; i < expectedOutputLines.length; i++) {

            assertTrue("insufficient lines in output", compilationEngineOutput.hasNextLine());

            String actualOutputLine = compilationEngineOutput.nextLine();

            assertEquals("Line comparision failure at line " + i, expectedOutputLines[i], actualOutputLine);
        }

        assertTrue("Extra lines in output", !compilationEngineOutput.hasNextLine());

        compilationEngineOutput.close();
        outputFile.delete();
    }

    @Test
    public void compileVariableDeclaration() throws Exception {
        JackTokenizer tokenizer = new JackTokenizer("var Array a;\n" +
                "        var int length;\n" +
                "        var int i, sum;", true);
        File outputFile = new File("temp.xml");
        CompilationEngine compilationEngine = new CompilationEngine(tokenizer, outputFile);

        compilationEngine.compileVariableDeclaration();
        compilationEngine.compileVariableDeclaration();
        compilationEngine.compileVariableDeclaration();

        compilationEngine.close();

        Scanner compilationEngineOutput = new Scanner(outputFile);

        Scanner expectedOuput = new Scanner("<varDec>\n" +
                "  <keyword> var </keyword>\n" +
                "  <identifier> Array </identifier>\n" +
                "  <identifier> a </identifier>\n" +
                "  <symbol> ; </symbol>\n" +
                "</varDec>\n" +
                "<varDec>\n" +
                "  <keyword> var </keyword>\n" +
                "  <keyword> int </keyword>\n" +
                "  <identifier> length </identifier>\n" +
                "  <symbol> ; </symbol>\n" +
                "</varDec>\n" +
                "<varDec>\n" +
                "  <keyword> var </keyword>\n" +
                "  <keyword> int </keyword>\n" +
                "  <identifier> i </identifier>\n" +
                "  <symbol> , </symbol>\n" +
                "  <identifier> sum </identifier>\n" +
                "  <symbol> ; </symbol>\n" +
                "</varDec>");

        int i = 1;
        while (expectedOuput.hasNextLine()) {
            assertTrue("insufficient lines in output", compilationEngineOutput.hasNextLine());
            assertEquals("Line comparision failure at line " + i, expectedOuput.nextLine(), compilationEngineOutput.nextLine());
            i++;
        }

        assertTrue("Extra lines in output", !compilationEngineOutput.hasNextLine());

        compilationEngineOutput.close();
        outputFile.delete();
    }

}