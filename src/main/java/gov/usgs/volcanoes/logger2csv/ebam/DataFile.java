package gov.usgs.volcanoes.logger2csv.ebam;

public enum DataFile {
  EEPROM('E'), 
  CHANNEL_DESCRIPTOR('1'), 
  DATA_LOG('2'), 
  ERROR_LOG('3'), 
  DIAG_LOG('4');

  private char value;

  DataFile(char value) {
    this.value = value;
  }

  public String toString() {
    return String.valueOf(value);
  }
}