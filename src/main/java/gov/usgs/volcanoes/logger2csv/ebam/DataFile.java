package gov.usgs.volcanoes.logger2csv.ebam;

public enum DataFile {
  EEPROM('E', false), 
  CHANNEL_DESCRIPTOR('1', false), 
  DATA_LOG('2', true), 
  ERROR_LOG('3', false), 
  DIAG_LOG('4', false);

  public final char value;
  public final boolean hasHeader;

  DataFile(char value, boolean hasHeader) {
    this.value = value;
    this.hasHeader = hasHeader;
  }
}