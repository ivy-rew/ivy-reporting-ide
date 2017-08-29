package ch.ivyteam.ivy.reporting.internal.dataset.entry;

/**
 * The flattened representation of a role for the report.
 * @author jst
 * @since 17.07.2009
 */
public class RoleReportDataEntry
{

  /** The Role ID. */
  private String id;

  /** The Role Name. */
  private String name;

  /** The ID of the Parent Role. */
  private String parentId;

  /**
   * Constructor
   * @param _id
   * @param _name
   * @param _parentId
   */
  public RoleReportDataEntry(String _id, String _name, String _parentId)
  {
    this.id = _id;
    this.name = _name;
    this.parentId = _parentId;
  }

  /**
   * Returns the id
   * @return the id
   */
  public String getId()
  {
    return id;
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
   * Returns the parentId
   * @return the parentId
   */
  public String getParentId()
  {
    return parentId;
  }
}
