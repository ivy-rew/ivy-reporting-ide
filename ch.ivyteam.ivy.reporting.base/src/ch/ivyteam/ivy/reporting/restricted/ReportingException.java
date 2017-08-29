package ch.ivyteam.ivy.reporting.restricted;

/**
 * Standard exception thrown by reporting subsystem.
 * @author jst
 * @since 27.07.2009
 */
public class ReportingException extends Exception
{
  /**
   * Constructor
   */
  public ReportingException()
  {
  }

  /**
   * Constructor
   * @param message
   * @param cause
   */
  public ReportingException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * Constructor
   * @param message
   */
  public ReportingException(String message)
  {
    super(message);
  }

  /**
   * Constructor
   * @param cause
   */
  public ReportingException(Throwable cause)
  {
    super(cause);
  }

}
