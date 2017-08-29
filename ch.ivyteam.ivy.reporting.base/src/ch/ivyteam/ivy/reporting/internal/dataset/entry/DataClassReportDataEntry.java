package ch.ivyteam.ivy.reporting.internal.dataset.entry;

import ch.ivyteam.ivy.scripting.dataclass.IDataClass;

/**
 * The flattened representation of a data class for the report.
 * @author jst
 * @since 19.06.2009
 */
public class DataClassReportDataEntry
{

  /** The project report data entry. */
  private ProjectReportDataEntry project;

  /** The DataClass */
  private IDataClass dataClass;

  /** The Section of this DataClass. */
  private String dataClassSection;

  /** The Section of this DataClass namespace. */
  private String dcNamespaceSection;
  
  /** identifier */
  private int id;

  /**
   * Constructor
   * @param _project
   * @param _dataClass
   * @param _dataClassSection
   * @param _dcNamespaceSection
   * @param _id 
   */
  public DataClassReportDataEntry(ProjectReportDataEntry _project, IDataClass _dataClass,
          String _dataClassSection, String _dcNamespaceSection, int _id)
  {
    this.project = _project;
    this.dataClass = _dataClass;
    this.dataClassSection = _dataClassSection;
    this.dcNamespaceSection = _dcNamespaceSection;
    this.id = _id;
  }

  /**
   * Returns the project
   * @return the project
   */
  public ProjectReportDataEntry getProject()
  {
    return project;
  }

  /**
   * Returns the dataClass
   * @return the dataClass
   */
  public IDataClass getDataClass()
  {
    return dataClass;
  }

  /**
   * Returns the dataClassSection
   * @return the dataClassSection
   */
  public String getDataClassSection()
  {
    return dataClassSection;
  }

  /**
   * Returns the dcNamespaceSection
   * @return the dcNamespaceSection
   */
  public String getDcNamespaceSection()
  {
    return dcNamespaceSection;
  }
  
  /**
   * Gets the id
   * @return id
   */
  public int getId()
  {
    return id;
  }

}
