package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.datawrapper.DataWrapperFactory;
import ch.ivyteam.ivy.datawrapper.DataWrapperNode;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProcessReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.RichDialogReportDataEntry;

/**
 * Scripted Data Set for Process Element Attributes.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
// TODO jst: Get the ImageMaps to work, such that the document can be
// navigated using the process image. (At least for HTML)
public class ProcessElementAttributeDataSetEventAdapter extends ReportingDataSetEventAdapter
{
  /** The map with the attributes */
  private Iterator<Map.Entry<String, String>> attributeIterator;
  /** The next identifier */
  private int nextIdentifier = 0;
  
  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    try
    {
      Integer processElementId;
      processElementId = (Integer) dataSet.getInputParameterValue("ProcessElementId");
      for (ProcessReportDataEntry entry : reportDataCollector.getProcessReportData())
      {
        if (((Integer)entry.getId()).equals(processElementId))
        {
          /* Get Process Element Inscription mask details. */
          DataWrapperNode<?> elementDW = (DataWrapperNode<?>)
                  DataWrapperFactory.createDataWrapper(entry.getProcessElement(), entry.getProject().getIvyProject());
          Map<String, String> attributes = new LinkedHashMap<String, String>();
          elementDW.getCompleteInfo(attributes);
          attributeIterator = attributes.entrySet().iterator();
          nextIdentifier = 0;
          return;
        }
      }
      for (RichDialogReportDataEntry entry : reportDataCollector.getRichDialogReportData())
      {
        if (((Integer)entry.getProcessId()).equals(processElementId))
        {
          /* Get Process Element Inscription mask details. */
          DataWrapperNode<?> elementDW = (DataWrapperNode<?>)
                  DataWrapperFactory.createDataWrapper(entry.getProcessElement(), entry.getProject().getIvyProject());
          Map<String, String> attributes = new LinkedHashMap<String, String>();
          elementDW.getCompleteInfo(attributes);
          attributeIterator = attributes.entrySet().iterator();
          nextIdentifier = 0;
          return;
        }
      }
    }
    catch (ScriptException ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#fetch(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow)
   */
  @Override
  public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row)
  {
    if ((attributeIterator == null)||(attributeIterator.hasNext()==false))
    {
      return false;
    }
    
    Map.Entry<String, String> entry = attributeIterator.next();
    try
    {
      row.setColumnValue("Id", nextIdentifier++);
      row.setColumnValue("Name", entry.getKey());
      row.setColumnValue("Value", entry.getValue());
    }
    catch (ScriptException ex)
    {
      ReportingManager.getLogger().error("Error filling Report Data.", ex);
      throw new RuntimeException("Error filling Report Data.", ex);
    }
    return true;
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    attributeIterator = null;
    nextIdentifier = 0;
  }
}
