package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.application.IProcessModel;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;

/**
 * Scripted Data Set for Applications, ProcessModels, ProcessModelVersions and IvyProjects.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class ProjectDataSetEventAdapter extends ReportingDataSetEventAdapter
{
  /** A iterator through the project report data entries. */
  private Iterator<ProjectReportDataEntry> projIt;

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance _dataSet)
  {
    projIt = reportDataCollector.getProjectReportData().iterator();
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#fetch(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow)
   */
  @Override
  public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row)
  {
    /* Fill the data set. */
    if (projIt.hasNext())
    {
      ProjectReportDataEntry entry = projIt.next();
      try
      {
        // Application data
        IApplication app = entry.getApplication();
        row.setColumnValue("ApplicationName", app.getName());
        row.setColumnValue("ApplicationDescription", app.getDescription());
        row.setColumnValue("ApplicationState", app.getActivityStateText());
        row.setColumnValue("ApplicationOperationState", app.getActivityOperationStateText());
        row.setColumnValue("ApplicationDirectory", app.getFileDirectory());

        // Process model data
        IProcessModel pm = entry.getProcessModel();
        row.setColumnValue("ProcessModelName", pm.getName());
        row.setColumnValue("ProcessModelDescription", pm.getDescription());

        // Process model version data
        IProcessModelVersion pmv = entry.getProcessModelVersion();
        row.setColumnValue("VersionNumber", Integer.toString(pmv.getVersionNumber()));
        row.setColumnValue("VersionDescription", pmv.getDescription());
        row.setColumnValue("VersionChangedBy", pmv.getLastChangeBy());
        row.setColumnValue("VersionChangeDate", pmv.getLastChangeDate());
        row.setColumnValue("VersionChangedFromHost", pmv.getLastChangeFromHost());
        row.setColumnValue("VersionReleaseState", pmv.getReleaseState());
        row.setColumnValue("VersionReleaseTimestamp", pmv.getReleaseTimestamp());
        row.setColumnValue("VersionName", pmv.getVersionName());

        // Project Section
        row.setColumnValue("ProjectNumber", entry.getProjSection() + ".");
      }
      catch (ScriptException ex)
      {
        ReportingManager.getLogger().error("Error filling Report Data.", ex);
        throw new RuntimeException("Error filling Report Data.", ex);
      }
      catch (PersistencyException ex)
      {
        ReportingManager.getLogger().error("Error getting Report Data.", ex);
        throw new RuntimeException("Error getting Report Data.", ex);
      }

      return true;
    }
    return false;
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    projIt = null;
  }
}
