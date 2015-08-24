package gov.usgs.volcanoes.logger2csv;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Argument processor for Logger2csv
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Args extends SimpleJSAP {

    public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";
    public static final String PROGRAM_NAME = "java -jar gov.usgs.volcanes.logger2csv.Logger2csv";
    public static final String EXPLANATION = "I am the logger2csv server\n";

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Switch("create-config", 'c', "create-config",
                    "Create an example config file in the curent working directory."),
            new Switch("persistent", 'p', "persistent", "Run persistenly, periodically polling loggers."),
            new Switch("verbose", 'v', "verbose", "Verbose logging."),
            new UnflaggedOption("config-filename", JSAP.STRING_PARSER, DEFAULT_CONFIG_FILENAME, JSAP.NOT_REQUIRED,
                    JSAP.NOT_GREEDY, "The config file name.") };

    private JSAPResult config;
    public final boolean createConfig;
    public final String configFileName;
    public final boolean verbose;
    public final boolean persistent;

    public Args(String[] args) throws JSAPException {
        super(PROGRAM_NAME, EXPLANATION, PARAMETERS);
        config = parse(args);
        if (messagePrinted()) {
            if (!config.getBoolean("help"))
                System.err.println("Try using the --help flag.");

            System.exit(1);
        }

        createConfig = config.getBoolean("create-config");
        configFileName = config.getString("config-filename");
        verbose = config.getBoolean("verbose");
        persistent = config.getBoolean("daemon");
    }
}