package ch.ivyteam.ivy.reporting.internal.dataset.entry;

/**
 * The flattened representation of a web service for the report.
 * @author jst
 * @since 17.07.2009
 */
public class WebServiceReportDataEntry
{
  /** The WS Config name */
  private String name;

  /** The WS Config description */
  private String description;

  /** The WS Config wsdl file */
  private String wsdlFile;

  /** The Environment of the WSConfig */
  private String environment;

  /** The Endpoint for this Environment */
  private String endpoint;

  /**
   * Constructor
   * @param _name
   * @param _description
   * @param _wsdlFile
   * @param _environment
   * @param _endpoint
   */
  public WebServiceReportDataEntry(String _name, String _description, String _wsdlFile, String _environment,
          String _endpoint)
  {
    this.name = _name;
    this.description = _description;
    this.wsdlFile = _wsdlFile;
    this.environment = _environment;
    this.endpoint = _endpoint;
  }

  /**
   * Returns the name
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the description
   * @return the description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the wsdlFile
   * @return the wsdlFile
   */
  public String getWsdlFile()
  {
    return wsdlFile;
  }

  /**
   * Returns the environment
   * @return the environment
   */
  public String getEnvironment()
  {
    return environment;
  }

  /**
   * Returns the endpoint
   * @return the endpoint
   */
  public String getEndpoint()
  {
    return endpoint;
  }

}
