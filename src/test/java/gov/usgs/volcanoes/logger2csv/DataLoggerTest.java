package gov.usgs.volcanoes.logger2csv;

public class DataLoggerTest {

  // private ConfigFile config;
  // private DataLogger logger;
  //
  // @Before
  // public void setup() throws IOException {
  // config = new ConfigFile();
  // config.put("name", "testConfig");
  // config.put("address", "127.0.0.1");
  // config.put("table", "testTable");
  // logger = new DataLogger(config);
  // }
  //
  // @Test
  // public void when_givenDate_then_returnDate() throws ParseException {
  // Calendar cal = Calendar.getInstance();
  // cal.setTimeZone(TimeZone.getTimeZone("UTC"));
  // cal.set(2015, 8-1, 1, 1, 2, 3);
  // cal.set(Calendar.MILLISECOND, 0);
  //
  // Date calculatedDate = logger.parseDate("2015-08-01 01:02:03");
  // assertEquals(cal.getTime(), calculatedDate);
  // }
  //
  // @Test(expected = IOException.class)
  // public void when_noName_then_throwHelpfulException() throws IOException {
  // ConfigFile myConfig = config.deepCopy();
  // myConfig.putList("name", null);
  // new DataLogger(myConfig);
  // }
  //
  // @Test(expected = IOException.class)
  // public void when_noAddress_then_throwHelpfulException() throws IOException {
  // ConfigFile myConfig = config.deepCopy();
  // myConfig.putList("address", null);
  // new DataLogger(myConfig);
  // }
  //
  // @Test(expected = IOException.class)
  // public void when_noTable_then_throwHelpfulException() throws IOException {
  // ConfigFile myConfig = config.deepCopy();
  // myConfig.putList("table", null);
  // new DataLogger(myConfig);
  // }
  //
  // @Test
  // public void returnIterator() {
  // Iterator<String> it = logger.getTableIterator();
  //
  // // There is always at least one table
  // assertTrue(it.hasNext());
  // }
  //
  // @Test
  // public void getFilename() {
  // Calendar cal = Calendar.getInstance();
  // cal.setTimeZone(TimeZone.getTimeZone("UTC"));
  // cal.set(2015, 8-1, 1, 1, 2, 3);
  // cal.set(Calendar.MILLISECOND, 0);
  //
  // long time = cal.getTimeInMillis();
  // String expectedName = "data/testConfig/2015/08/testConfig-testTable-20150801";
  // // There is always at least one table
  // assertEquals(expectedName, logger.getFileName("testTable", time));
  // }
}
