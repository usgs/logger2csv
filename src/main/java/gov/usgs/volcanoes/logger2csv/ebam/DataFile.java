/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;

/**
 * Queryable data files on a eBAM.
 * 
 * @author Tom Parker
 *
 */
public enum DataFile {
  /** EEPROM */
  EEPROM('E', false),

  /** CHANNEL_DESCRIPTOR */
  CHANNEL_DESCRIPTOR('1', false),

  /** DATA LOG */
  DATA_LOG('2', true),

  /** ERROR LOG */
  ERROR_LOG('3', false),

  /** DIAG LOG */
  DIAG_LOG('4', false);

  /**
   * Value assigned to data file by eBAM. Described in section 9.3
   * "Advanced Communications – Escape Commands" of the operation manual Rev. M
   */
  public final char value;
  
  /** If true, this data file includes a headder row */
  public final boolean hasHeader;

  DataFile(final char value, final boolean hasHeader) {
    this.value = value;
    this.hasHeader = hasHeader;
  }
}
