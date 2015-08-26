package gov.usgs.volcanoes.logger2csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.Switch;

import gov.usgs.volcanoes.util.args.Args;
import gov.usgs.volcanoes.util.args.Arguments;
import gov.usgs.volcanoes.util.args.decorator.ConfigFileArg;
import gov.usgs.volcanoes.util.args.decorator.CreateConfigArg;

/**
 * Argument processor for Logger2csv
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Logger2csvArgs {
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csvArgs.class);

	public static final String EXAMPLE_CONFIG_FILENAME = "logger2csv-example.config";
	public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";
	public static final String PROGRAM_NAME = "java -jar gov.usgs.volcanes.logger2csv.Logger2csv";
	public static final String EXPLANATION = "I am the logger2csv server\n";

	private static final Parameter[] PARAMETERS = new Parameter[] {
			new Switch("persistent", 'p', "persistent", "Run persistenly, periodically polling loggers."), };

	public final boolean persistent;
	public final String configFileName;

	public Logger2csvArgs(String[] commandLineArgs) {
		Arguments args = new Args(PROGRAM_NAME, EXPLANATION, PARAMETERS);
		try {
			args = new ConfigFileArg(DEFAULT_CONFIG_FILENAME, args);
			args = new CreateConfigArg(EXAMPLE_CONFIG_FILENAME, args);
		} catch (JSAPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSAPResult jsapResult = null;
		try {
			jsapResult = args.parse(commandLineArgs);
		} catch (ParseException e) {
			LOGGER.error("Cannot parse command line.");
			System.exit(1);
		}

		persistent = jsapResult.getBoolean("persistent");
        LOGGER.debug("Setting: persistent={}", persistent);

        configFileName = jsapResult.getString("config-filename");
        LOGGER.debug("Setting: configFileName={}", configFileName);
	}
}
