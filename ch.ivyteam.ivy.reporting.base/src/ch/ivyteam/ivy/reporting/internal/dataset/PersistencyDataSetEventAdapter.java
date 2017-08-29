package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.process.data.persistence.ProcessDataPersistenceUtil;
import ch.ivyteam.ivy.process.data.persistence.datamodel.IProcessDataPersistenceConfig;
import ch.ivyteam.ivy.process.data.persistence.model.Persistence.PersistenceUnit;
import ch.ivyteam.ivy.process.data.persistence.model.Persistence.PersistenceUnit.Properties.Property;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.search.restricted.ProjectRelationSearchScope;

/**
 * Scripted Data Set for Persistency.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 31.8.2009
 */
public class PersistencyDataSetEventAdapter extends ReportingDataSetEventAdapter
{

  /** An iterator through the persistency configuration units. */
  private Iterator<PersistenceUnit> pcIt;

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
          List<PersistenceUnit> allPersistenceConfigs = new LinkedList<PersistenceUnit>();
          Set<IProcessDataPersistenceConfig> persistenceConfigs = proj.getIvyProject()
                  .getProjectProcessDataPersistenceConfigManager().getDataModels(ProjectRelationSearchScope.CURRENT_AND_ALL_REQUIRED_PROJECTS, null).getModels();
          for (IProcessDataPersistenceConfig config : persistenceConfigs)
          {
            allPersistenceConfigs.addAll(config.getPersistenceUnitConfigs());
          }
          pcIt = allPersistenceConfigs.iterator();
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
    catch (ResourceDataModelException ex)
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
    if (pcIt != null && pcIt.hasNext())
    {
      PersistenceUnit persistenceUnit = pcIt.next();
      try
      {
        row.setColumnValue("Name", persistenceUnit.getName());
        row.setColumnValue("Description", persistenceUnit.getDescription());
        String dataSource = ProcessDataPersistenceUtil.getDataSourceName(persistenceUnit);
        row.setColumnValue("DataSource", dataSource);
        row.setColumnValue("Provider", persistenceUnit.getProvider());

        StringBuffer sb = new StringBuffer();
        for (Property p : persistenceUnit.getProperties().getProperty())
        {
          sb.append(p.getName());
          sb.append(": ");
          sb.append(p.getValue());
          sb.append("\n");
        }
        row.setColumnValue("Properties", sb.toString());

      }
      catch (ScriptException ex)
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
    pcIt = null;
  }
}
