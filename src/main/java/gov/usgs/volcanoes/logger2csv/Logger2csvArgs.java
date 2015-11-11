/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;

import gov.usgs.volcanoes.core.args.Args;
import gov.usgs.volcanoes.core.args.ArgumentException;
import gov.usgs.volcanoes.core.args.Arguments;
import gov.usgs.volcanoes.core.args.decorator.ConfigFileArg;
import gov.usgs.volcanoes.core.args.decorator.CreateConfigArg;
import gov.usgs.volcanoes.core.args.decorator.VerboseArg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Argument processor for Logger2csv
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide through the CC0 1.0
 *         Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Logger2csvArgs {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csvArgs.class);


  /** Default config file name */
  public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";

  private static final String EXAMPLE_CONFIG_FILENAME = "logger2csv-example.config";
  private static final String PROGRAM_NAME = "java -jar gov.usgs.volcanes.logger2csv.Logger2csv";
  private static final String EXPLANATION = "I am the logger2csv server\n";

  private static final Parameter[] PARAMETERS = new Parameter[] {new Switch("persistent", 'p',
      "persistent", "Run persistenly, periodically polling loggers.")};

  /** Run persistently? */
  public final boolean persistent;
  
  /** configuration file name */
  public final String configFileName;
  
  /** Okay to start application */
  public final boolean runnable;

  /**
   * Parse my apps command line arguments.
   * 
   * @param commandLineArgs the arg array passed to main()
   * @throws ArgumentException if something goes wrong
   */
  public Logger2csvArgs(final String... commandLineArgs) throws ArgumentException {
    Arguments args = new Args(PROGRAM_NAME, EXPLANATION, PARAMETERS);
    args = new ConfigFileArg(DEFAULT_CONFIG_FILENAME, args);
    args = new CreateConfigArg(EXAMPLE_CONFIG_FILENAME, args);
    args = new VerboseArg(args);

    final JSAPResult jsapResult = args.parse(commandLineArgs);

    persistent = jsapResult.getBoolean("persistent");
    LOGGER.debug("Setting: persistent={}", persistent);

    configFileName = jsapResult.getString("config-filename");
    LOGGER.debug("Setting: configFileName={}", configFileName);

    runnable = !(args.messagePrinted() || jsapResult.getBoolean("create-config"));
  }
}
