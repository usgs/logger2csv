package gov.usgs.volcanoes.logger2csv;

public abstract class Poller {
  public static final long M_TO_S = 60;
  public static final long DAY_TO_S = 24 * 60 * M_TO_S;
  public static final long M_TO_MS = 60 * 1000;

  protected String[] headers;
  
  abstract public void poll();
  
  public String[] getHeader() {
    if (headers == null)
      throw new IllegalStateException("poll() must be called before getHeader()");

    return headers;
  }
}
