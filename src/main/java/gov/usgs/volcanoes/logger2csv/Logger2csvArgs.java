package gov.usgs.volcanoes.logger2csv;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import gov.usgs.volcanoes.util.args.Args;

/**
 * Argument processor for Logger2csv
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Logger2csvArgs extends Args {
    
    public static final String EXAMPLE_CONFIG_FILENAME = "logger2csv-example.config";
    public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";
    public static final String PROGRAM_NAME = "java -jar gov.usgs.volcanes.logger2csv.Logger2csv";
    public static final String EXPLANATION = "I am the logger2csv server\n";

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Switch("persistent", 'p', "persistent", "Run persistenly, periodically polling loggers."),
            new Switch("verbose", 'v', "verbose", "Verbose logging.") 
            };

    public final boolean verbose;
    public final boolean persistent;

    public Logger2csvArgs(String[] args) {
        super(PROGRAM_NAME, EXPLANATION, PARAMETERS);
        addCreateConfig(EXAMPLE_CONFIG_FILENAME, DEFAULT_CONFIG_FILENAME);
        config = parse(args);
        
        verbose = config.getBoolean("verbose");
        persistent = config.getBoolean("persistent");
    }
}