package de.fosd.typechef;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprParser;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.lexer.options.LexerOptions;
import de.fosd.typechef.lexer.options.OptionException;
import de.fosd.typechef.lexer.options.Options;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.List;


public class FrontendOptions extends LexerOptions {
    boolean parse = true,
            typecheck = true,
            writeInterface = true,
            conditionalControlFlow = false,
            serializeAST = false,
            writeDebugInterface = false,
            recordTiming = false,
            parserStatistics = false,
            writePI = false;
    String outputStem = "";
    private String filePresenceConditionFile = "";


    private final static char F_PARSE = Options.genOptionId();
    private final static char F_INTERFACE = Options.genOptionId();
    private final static char F_WRITEPI = Options.genOptionId();
    private final static char F_DEBUGINTERFACE = Options.genOptionId();
    private final static char F_CONDITIONALCONTROLFLOW = Options.genOptionId();
    private final static char F_SERIALIZEAST = Options.genOptionId();
    private final static char F_RECORDTIMING = Options.genOptionId();
    private final static char F_FILEPC = Options.genOptionId();
    private final static char F_PARSERSTATS = Options.genOptionId();

    @Override
    protected List<Options.OptionGroup> getOptionGroups() {
        List<OptionGroup> r = super.getOptionGroups();

        r.add(new OptionGroup("General processing options (lexing, parsing, type checking, interfaces; select only highest)", 10,
                new Option("lex", LongOpt.NO_ARGUMENT, 'E', null,
                        "Stop after lexing; no parsing."),
                new Option("parse", LongOpt.NO_ARGUMENT, F_PARSE, null,
                        "Lex and parse the file; no type checking."),
                new Option("typecheck", LongOpt.NO_ARGUMENT, 't', null,
                        "Lex, parse, and type check; but do not create interfaces."),
                new Option("interface", LongOpt.NO_ARGUMENT, F_INTERFACE, null,
                        "Lex, parse, type check, and create interfaces (default)."),

                new Option("conditionalControlFlow", LongOpt.NO_ARGUMENT, F_CONDITIONALCONTROLFLOW, null,
                        "Lex, parse, and check conditional control flow"),

                new Option("output", LongOpt.REQUIRED_ARGUMENT, 'o', "file",
                        "Path to output files (no extension, creates .pi, .macrodbg etc files)."),

                new Option("writePI", LongOpt.NO_ARGUMENT, F_WRITEPI, null,
                        "Write lexer output into .pi file"),
                new Option("debugInterface", LongOpt.NO_ARGUMENT, F_DEBUGINTERFACE, null,
                        "Write interface in human readable format (requires --interface)"),

                new Option("serializeAST", LongOpt.NO_ARGUMENT, F_SERIALIZEAST, null,
                        "Write ast to .ast file after parsing."),
                new Option("recordTiming", LongOpt.NO_ARGUMENT, F_RECORDTIMING, null,
                        "Report times for all phases."),

                new Option("filePC", LongOpt.REQUIRED_ARGUMENT, F_FILEPC, "file",
                        "Presence condition for the file (format like --featureModelFExpr). Default 'file.pc'.")
        ));
        r.add(new OptionGroup("Parser options", 23,
                new Option("parserstatistics", LongOpt.NO_ARGUMENT, F_PARSERSTATS, null,
                        "Print parser statistics.")
        ));

        return r;

    }

    @Override
    protected boolean interpretOption(int c, Getopt g) throws OptionException {
        if (c == 'E') {       //--lex
            parse = typecheck = writeInterface = false;
            lexPrintToStdout = true;
        } else if (c == F_PARSE) {//--parse
            parse = true;
            typecheck = writeInterface = false;
        } else if (c == 't') {//--typecheck
            parse = typecheck = true;
            writeInterface = false;
        } else if (c == F_INTERFACE) {//--interface
            parse = typecheck = writeInterface = true;
        } else if (c == F_CONDITIONALCONTROLFLOW) {//--conditional control flow check
            parse = conditionalControlFlow = true;
        } else if (c == F_SERIALIZEAST) {
            serializeAST = true;
        } else if (c == F_RECORDTIMING) {
            recordTiming = true;
        } else if (c == F_DEBUGINTERFACE) {
            writeDebugInterface = true;
        } else if (c == 'o') {
            outputStem = g.getOptarg();
        } else if (c == F_FILEPC) {//--filePC
            checkFileExists(g.getOptarg());
            filePresenceConditionFile = g.getOptarg();
        } else if (c == F_PARSERSTATS) {
            parserStatistics = true;
        } else if (c == F_WRITEPI) {
            writePI = true;
        } else
            return super.interpretOption(c, g);

        return true;

    }

    protected void afterParsing() throws OptionException {
        super.afterParsing();
        if (getFiles().size() <= 0)
            throw new OptionException("No file specified.");
        if (getFiles().size() > 1)
            throw new OptionException("Multiple files specified. Only one supported.");

        if (outputStem.length() == 0)
            outputStem = getFile().replace(".c", "");
        if (writePI && (lexOutputFile == null || lexOutputFile.length() == 0))
            lexOutputFile = outputStem + ".pi";
    }

    String getFile() {
        return getFiles().iterator().next();
    }

    String getInterfaceFilename() {
        return outputStem + ".interface";
    }

    String getDebugInterfaceFilename() {
        return outputStem + ".dbginterface";
    }

    String getFilePresenceConditionFilename() {
        if (filePresenceConditionFile.length() > 0)
            return filePresenceConditionFile;
        else
            return outputStem + ".pc";
    }

    private FeatureExpr filePC = null;

    FeatureExpr getFilePresenceCondition() {
        if (filePC == null)
            filePC = new FeatureExprParser().parseFile(getFilePresenceConditionFilename());
        return filePC;
    }

    String getLocalFeatureModelFilename() {
        return outputStem + ".fm";
    }

    private FeatureExpr localFM = null;

    FeatureExpr getLocalFeatureModel() {
        if (localFM == null)
            localFM = new FeatureExprParser().parseFile(getLocalFeatureModelFilename());
        return localFM;
    }

    String getSerializedASTFilename() {
        return outputStem + ".ast";
    }
}
