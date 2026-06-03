package de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception;

/**
 * Exception wrapper for several exceptions that can be thrown, including a flag, if the exception is caused by a dependent service ({@link HSPException#isCausedByDependence})
 */
public class HSPException extends Exception {

  /**
   * id for serializing the object
   */
  private static final long serialVersionUID = 1L;

  /* indicates if an exception is caused by a missing / not working dependence */
  protected final boolean isCausedByDependence;

  /* indicates whether this exception should lead to stop consuming messages */
  protected final boolean isCritical;

  public HSPException(final String message, final boolean isCausedByDependence, final boolean isCritical) {
    super(message);
    this.isCausedByDependence = isCausedByDependence;
    this.isCritical = isCritical;
  }

  public HSPException(final String message, final Exception ex, final boolean isCausedByDependence, final boolean isCritical) {
    super(message, ex);
    this.isCausedByDependence = isCausedByDependence;
    this.isCritical = isCritical;
  }

  public HSPException(final String message) {
    this(message, false, false);
  }


  public HSPException(final String message, final Exception ex) {
    this(message, ex, false, false);
  }

  /**
   * indicates if the exception is caused by a dependent (external) service
   *
   * @return true if exception is caused by a dependent service, false otherwise
   */
  public boolean isCausedByDependence() {
    return isCausedByDependence;
  }

  /**
   * indicates if the exception should lead to stop consuming messages
   *
   * @return true if exception is critical, false otherwise
   */
  public boolean isCritical() {
    return isCritical;
  }
}
