package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.WebServiceReportDataEntry;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.webservice.datamodel.IWebServiceConfiguration;
import ch.ivyteam.ivy.webservice.datamodel.IWebServiceEnvironmentConfiguration;
import ch.ivyteam.ivy.webservice.datamodel.IWebServicePort;

/**
 * Scripted Data Set for Web Services.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class WebServiceDataSetEventAdapter extends ReportingDataSetEventAdapter
{

  /** A iterator through the WebService Configurations. */
  private Iterator<WebServiceReportDataEntry> wsIt;

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
          LinkedList<WebServiceReportDataEntry> wsList = new LinkedList<WebServiceReportDataEntry>();
          for (IWebServiceConfiguration wsConfig : proj.getIvyProject().getWebServiceConfigurations(null))
          {
            for (IWebServiceEnvironmentConfiguration envConfig : wsConfig.getEnvironments())
            {
              wsList.add(new WebServiceReportDataEntry(wsConfig.getName(), wsConfig.getWebServiceInfo().getDescription(),
                      wsConfig.getWebServiceInfo().getWsdlUrl(), envConfig.getName(), getFirstEndpoint(envConfig)));
            }
          }
          wsIt = wsList.iterator();
          break;
        }
      }
    }
    catch (ScriptException ex)
    {
      ex.printStackTrace();
    }
    catch (PersistencyException ex)
    {
      ex.printStackTrace();
    }
    catch (ResourceDataModelException ex)
    {
      ex.printStackTrace();
    }
  }

  private String getFirstEndpoint(IWebServiceEnvironmentConfiguration envConfig)
  {
    for (IWebServicePort port : envConfig.getPorts())
    {
      if (StringUtils.isNotBlank(port.getDefaultEndpoint()))
      {
        return port.getDefaultEndpoint();
      }
    }
    return "";  
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#fetch(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow)
   */
  @Override
  public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row)
  {

    /* Fill the data set. */
    if (wsIt != null && wsIt.hasNext())
    {
      WebServiceReportDataEntry wsEntry = wsIt.next();
      try
      {
        row.setColumnValue("WSName", wsEntry.getName());
        row.setColumnValue("WSDesc", wsEntry.getDescription());
        row.setColumnValue("WSWSDL", wsEntry.getWsdlFile());
        row.setColumnValue("WSEnvironment", wsEntry.getEnvironment());
        row.setColumnValue("WSEndpoint", wsEntry.getEndpoint());
      }
      catch (ScriptException ex)
      {
        ReportingManager.getLogger().error("Error filling Report Data.", ex);
        throw new RuntimeException("Error filling Report Data.", ex);
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
    wsIt = null;
  }
}
