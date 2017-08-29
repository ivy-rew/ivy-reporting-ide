package ch.ivyteam.ivy.reporting.internal.dataset.entry;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.application.IProcessModel;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.project.IIvyProject;

/**
 * The flattened representation of a project for the report.
 * @author jst
 * @since 19.06.2009
 */
public class ProjectReportDataEntry
{

  /** The application. */
  private IApplication application;

  /** The process model. */
  private IProcessModel processModel;

  /** The process model version. */
  private IProcessModelVersion processModelVersion;

  /** The ivyProject. */
  private IIvyProject ivyProject;

  /** The project section number. */
  private String projSection;

  /**
   * Constructor
   * @param _application
   * @param _processModel
   * @param _processModelVersion
   * @param _ivyProject
   * @param _projSection
   */
  public ProjectReportDataEntry(IApplication _application, IProcessModel _processModel,
          IProcessModelVersion _processModelVersion, IIvyProject _ivyProject, String _projSection)
  {
    this.application = _application;
    this.processModel = _processModel;
    this.processModelVersion = _processModelVersion;
    this.ivyProject = _ivyProject;
    this.projSection = _projSection;
  }

  /**
   * Returns the application
   * @return the application
   */
  public IApplication getApplication()
  {
    return application;
  }

  /**
   * Returns the processModel
   * @return the processModel
   */
  public IProcessModel getProcessModel()
  {
    return processModel;
  }

  /**
   * Returns the processModelVersion
   * @return the processModelVersion
   */
  public IProcessModelVersion getProcessModelVersion()
  {
    return processModelVersion;
  }

  /**
   * Returns the ivyProject
   * @return the ivyProject
   */
  public IIvyProject getIvyProject()
  {
    return ivyProject;
  }

  /**
   * Returns the projSection
   * @return the projSection
   */
  public String getProjSection()
  {
    return projSection;
  }
}
