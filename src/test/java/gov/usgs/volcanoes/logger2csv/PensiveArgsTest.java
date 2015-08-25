package gov.usgs.volcanoes.logger2csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.martiansoftware.jsap.JSAPException;


public class PensiveArgsTest {

    private static final String CONFIG_FILENAME = "test.config";
    
    private static String[] commandLineArgs = {"-c", "-p", CONFIG_FILENAME};

    private Logger2csvArgs args;
    
    @Before
    public void setup() throws JSAPException {
        args = new Logger2csvArgs(commandLineArgs);
    }
    
    @Test
    public void requestExampleConfig() throws JSAPException {
        assertTrue(args.createConfig);
    }
    
    @Test
    public void requestPersistent() throws JSAPException {
        assertTrue(args.persistent);
    }
    
    @Test
    public void setConfigFile() throws JSAPException {
        assertEquals(CONFIG_FILENAME, args.configFileName);
    }
}




