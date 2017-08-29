package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.ComparatorUtils;
import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.db.jdbc.ConnectionProperty;
import ch.ivyteam.ivy.database.IExternalDatabaseConfiguration;
import ch.ivyteam.ivy.database.IExternalDatabaseDefaultConfiguration;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;

/**
 * Scripted Data Set for Databases.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class DatabaseDataSetEventAdapter extends ReportingDataSetEventAdapter
{
  private static final Comparator<String> NULL_SAFE_STRING_COMPARATOR = ComparatorUtils.nullLowComparator(ComparatorUtils.<String>naturalComparator());
  /** An iterator over the Database Configurations for the current Project. */
  private Iterator<IExternalDatabaseConfiguration> dbIt;

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
          // Get all the DB configurations (that is Default & Environments)
          List<IExternalDatabaseConfiguration> allDbConfigs = new LinkedList<IExternalDatabaseConfiguration>();
          List<IExternalDatabaseDefaultConfiguration> defaultDbConfigs = sortExternalDatabases(proj.getIvyProject()
                  .getExternalDatabases(null));
          for (IExternalDatabaseDefaultConfiguration defaultDbConfig : defaultDbConfigs)
          {
            allDbConfigs.add(defaultDbConfig);
            Map<String, IExternalDatabaseConfiguration> envDbConfigs = defaultDbConfig
                    .getEnvironmentConfigurations();
            for (IExternalDatabaseConfiguration environmentDbConfig : envDbConfigs.values())
            {
              allDbConfigs.add(environmentDbConfig);
            }
          }
          dbIt = allDbConfigs.iterator();
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

  private List<IExternalDatabaseDefaultConfiguration> sortExternalDatabases(
          Set<IExternalDatabaseDefaultConfiguration> externalDatabases)
  {
    List<IExternalDatabaseDefaultConfiguration> databases = new ArrayList<>(externalDatabases);
    Collections.sort(databases, new Comparator<IExternalDatabaseDefaultConfiguration>(){

      @Override
      public int compare(IExternalDatabaseDefaultConfiguration o1, IExternalDatabaseDefaultConfiguration o2)
      {
        return NULL_SAFE_STRING_COMPARATOR.compare(o1.getUserFriendlyName(), o2.getUserFriendlyName());
      }

    });
    return databases;
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#fetch(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow)
   */
  @Override
  public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row)
  {

    /* Fill the data set. */
    if (dbIt != null && dbIt.hasNext())
    {
      IExternalDatabaseConfiguration dbConfig = dbIt.next();
      try
      {
        row.setColumnValue("DBFriendlyName", dbConfig.getUserFriendlyName());
        if (dbConfig.getDatabaseProduct() != null)
        {
          row.setColumnValue("DBProduct", dbConfig.getDatabaseProduct().getName());
        }
        if (dbConfig.getJdbcDriver() != null)
        {
          row.setColumnValue("DBDriver", dbConfig.getJdbcDriver().getName());
        }
        row.setColumnValue("DBMaxConn", dbConfig.getMaxConnections());
        String environment = (dbConfig.getEnvironment() == null) ? "Default" : dbConfig.getEnvironment();
        row.setColumnValue("DBEnvironment", environment);

        writeDbSpecificCodeToRow(row, dbConfig);
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
   * Write the code that is specific to a type of DB to the DataSetRow.
   * 
   * @param row
   * @param dbConfig
   * @throws ScriptException
   */
  private void writeDbSpecificCodeToRow(IUpdatableDataSetRow row, IExternalDatabaseConfiguration dbConfig)
          throws ScriptException
  {
    // Padding between property name and value
    final int PADDING = 30;
    StringBuffer properties = new StringBuffer();
    for (ConnectionProperty property : dbConfig.getConnectionProperties())
    {
      if (dbConfig.getConnectionPropertyValue(property) != null &&
              dbConfig.getConnectionPropertyValue(property).length() > 0 &&
              !property.getLabel().equalsIgnoreCase("Password")) // Hide passwords
      {
        properties.append(property.getLabel());
        properties.append(":");
        for (int pad = property.getLabel().length(); pad < PADDING; pad++)
        {
          properties.append(" ");
        }
        properties.append(dbConfig.getConnectionPropertyValue(property));
        properties.append("\n");
      }
    }
    row.setColumnValue("DBConnectionProps", properties.toString());
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    dbIt = null;
  }
}
