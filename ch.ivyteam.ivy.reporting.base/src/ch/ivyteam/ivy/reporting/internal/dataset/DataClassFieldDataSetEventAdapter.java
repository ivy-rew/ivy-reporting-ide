package ch.ivyteam.ivy.reporting.internal.dataset;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.DataClassReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.RichDialogReportDataEntry;
import ch.ivyteam.ivy.scripting.dataclass.DataClassFieldModifier;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;
import ch.ivyteam.ivy.scripting.dataclass.IDataClassField;

/**
 * Scripted Data Set for Data Classes.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class DataClassFieldDataSetEventAdapter extends ReportingDataSetEventAdapter
{
  /** The data class to provide the fields for */
  private IDataClass ivyDataClass;
  
  /** the position */
  private int pos = 0;
  
  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    try
    {
      Integer dataClassId = (Integer)dataSet.getInputParameterValue("IvyDataClassId");
      for (DataClassReportDataEntry entry : reportDataCollector.getDataClassReportData())
      {
        if (((Integer)entry.getId()).equals(dataClassId))
        {
          ivyDataClass = entry.getDataClass();     
          pos = 0;
          return;
        }
      }
      for (RichDialogReportDataEntry entry : reportDataCollector.getRichDialogReportData())
      {
        if (((Integer)entry.getDataClassId()).equals(dataClassId))
        {
          ivyDataClass = entry.getDataClass();     
          pos = 0;
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
    if ((ivyDataClass == null)||(pos >= ivyDataClass.getFields().size()))
    {
      // no more data
      return false;
    }
    
    try
    {
      writeDataClassFieldsToRow(row, ivyDataClass.getFields().get(pos++));
      return true;
    }
    catch (ScriptException ex)
    {
      ReportingManager.getLogger().error("Error filling Report Data.", ex);
      throw new RuntimeException("Error filling Report Data.", ex);
    }
  }



  /**
   * Write the fields of a Data Class into the DataSetRow.
   * 
   * @param row
   * @param field
   * @throws ScriptException
   */
  private void writeDataClassFieldsToRow(IUpdatableDataSetRow row, IDataClassField field)
          throws ScriptException
  {
    row.setColumnValue("Id", pos);
    row.setColumnValue("Name", field.getName());
    row.setColumnValue("Type", field.getType());
    row.setColumnValue("Comment", field.getComment());
    row.setColumnValue("Modifiers", DataClassFieldModifier.format(field.getModifiers()));
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    ivyDataClass = null;
    pos = 0;
  }
}
