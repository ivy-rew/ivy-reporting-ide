package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.DataClassReportDataEntry;
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
public class DataClassDataSetEventAdapter extends ReportingDataSetEventAdapter
{

  /** An iterator through the data class report data entries. */
  private Iterator<DataClassReportDataEntry> dcIt;

  /** The VersionName of the parent project. */
  private String parentVersionName;

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    dcIt = reportDataCollector.getDataClassReportData().iterator();
    try
    {
      parentVersionName = (String) dataSet.getInputParameterValue("parentVersionName");
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

    /* Fill the data set. */
    while (dcIt.hasNext())
    {

      DataClassReportDataEntry entry = dcIt.next();
      try
      {
        /* Get only the entries that match the parent. */
        String versionName = entry.getProject().getProcessModelVersion().getVersionName();
        if (parentVersionName.equals(versionName))
        {
          String dcNamespaceSection = entry.getProject().getProjSection() + ".3."
                + entry.getDcNamespaceSection();
          writeDataClassToRow(row, entry.getDataClass(), entry.getDataClassSection(), dcNamespaceSection, entry.getId(), "Id");
          return true;
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
    return false;
  }

  /**
   * Writes the information about data classes into the DataSetRow.
   * 
   * @param row
   * @param dataClass
   * @param dataClassSection
   * @param dcNamespaceSection
   * @param id 
   * @param idColumn 
   * @throws ScriptException
   */
  static void writeDataClassToRow(IUpdatableDataSetRow row, IDataClass dataClass, String dataClassSection,
          String dcNamespaceSection, int id, String idColumn) throws ScriptException
  {
    String namespace = dataClass.getNamespace();
    if (dcNamespaceSection != null)
    {
      namespace = dcNamespaceSection + ". " + namespace;
    }
    row.setColumnValue("DCNamespace", namespace);
    String name = dataClass.getSimpleName();
    if (dcNamespaceSection != null && dataClassSection != null)
    {
      name = dcNamespaceSection + "." + dataClassSection + ". " + name;
    }
    row.setColumnValue("DCName", name);
    row.setColumnValue("DCComment", dataClass.getClassComment());
    row.setColumnValue(idColumn, id);
    writeDataClassFieldsToRow(row, dataClass);
  }

  /**
   * Write the fields of a Data Class into the DataSetRow.
   * 
   * @param row
   * @param dataClass
   * @throws ScriptException
   */
  private static void writeDataClassFieldsToRow(IUpdatableDataSetRow row, IDataClass dataClass)
          throws ScriptException
  {
    final String marker = "* ";
    final String descSeparator = "\n";
    StringBuffer fields = new StringBuffer();
    for (IDataClassField field : dataClass.getFields())
    {
      fields.append(marker);
      fields.append(field.getName());
      fields.append(":");
      fields.append(field.getType());
      if (field.getComment() != null && field.getComment().length() > 0)
      {
        fields.append(descSeparator);
        fields.append(field.getComment());
      }
      fields.append("\n");
    }
    row.setColumnValue("DCFields", fields.toString());
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    dcIt = null;
  }
}
