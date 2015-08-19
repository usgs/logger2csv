package gov.usgs.volcanoes.logger2csv;

import java.util.Date;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Argument processor for Pensive
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Args extends SimpleJSAP {

    public static final String DEFAULT_CONFIG_FILENAME = "pensive.config";
    public static final String PROGRAM_NAME = "java -jar net.stash.pensive.Pensive";
    public static final String EXPLANATION = "I am the Pensive server\n";

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Switch("create-config", 'c', "create-config",
                    "Create an example config file in the curent working directory."),
            new Switch("verbose", 'v', "verbose", "Verbose logging."),
            new UnflaggedOption("config-filename", JSAP.STRING_PARSER, DEFAULT_CONFIG_FILENAME, JSAP.NOT_REQUIRED,
                    JSAP.NOT_GREEDY, "The config file name.") };

    private JSAPResult config;
    public final boolean createConfig;
    public final String configFileName;
    public final boolean verbose;
    public final Date startTime;
    public final Date endTime;

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

        startTime = config.getDate("startTime");
        endTime = config.getDate("endTime");
        if (!validateTimes())
            System.exit(1);
    }

    private boolean validateTimes() {
        if (startTime == null && endTime == null)
            return true;

        if (startTime != null && endTime == null) {
            System.err.println("endTime argument is missing");
            return false;
        }

        if (endTime != null && startTime == null) {
            System.err.println("startTime argument is missing");
            return false;
        }

        if (!endTime.after(startTime)) {
            System.err.println("startTime must be before endTime");
            return false;
        }
            
        return true;
    }
}