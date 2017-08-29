package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;

/**
 * Scripted Data Set for Dependencies.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class DependencyDataSetEventAdapter extends ReportingDataSetEventAdapter
{

  /** A iterator through the required projects by the current project. */
  private Iterator<IIvyProject> reqIt;

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    try
    {
      String parentVersionName = (String) dataSet.getInputParameterValue("parentVersionName");
      for (ProjectReportDataEntry proj : reportDataCollector.getProjectReportData())
      {
        if (proj.getProcessModelVersion().getVersionName().equals(parentVersionName))
        {
          reqIt = proj.getIvyProject().getRequiredProjects().iterator();
          break;
        }
      }
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
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#fetch(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow)
   */
  @Override
  public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row)
  {

    /* Fill the data set. */
    if (reqIt != null && reqIt.hasNext())
    {
      IIvyProject entry = reqIt.next();
      try
      {
        row.setColumnValue("ReqName", entry.getName());
      }
      catch (ScriptException ex)
      {
        ex.printStackTrace();
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
    reqIt = null;
  }
}
