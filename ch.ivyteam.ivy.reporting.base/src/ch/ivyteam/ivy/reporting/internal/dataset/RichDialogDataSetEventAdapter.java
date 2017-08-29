package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.components.ZObject;
import ch.ivyteam.ivy.dialog.configuration.ulc.UlcDialogUtil;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.RichDialogReportDataEntry;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.richdialog.config.IRichDialog;
import ch.ivyteam.ivy.richdialog.config.iface.IRdAcceptedBroadcastEventDesc;
import ch.ivyteam.ivy.richdialog.config.iface.IRdFiredEventDesc;
import ch.ivyteam.ivy.richdialog.config.iface.IRdMethodDesc;

/**
 * Scripted Data Set for RichDialogs including Namespace, Interface, Processes, Data classes.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class RichDialogDataSetEventAdapter extends ReportingDataSetEventAdapter
{

  /** A iterator through the process report data entries. */
  private Iterator<RichDialogReportDataEntry> rdIt;

  /** The VersionName of the parent project. */
  private String parentVersionName;

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    rdIt = reportDataCollector.getRichDialogReportData().iterator();
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
    if (rdIt != null && rdIt.hasNext())
    {
      try
      {
        RichDialogReportDataEntry entry = rdIt.next();

        /* Get only the entries that match the parent. */
        String versionName = entry.getProject().getProcessModelVersion().getVersionName();
        while (!parentVersionName.equals(versionName))
        {
          if (!rdIt.hasNext())
          {
            return false;
          }
          entry = rdIt.next();
          versionName = entry.getProject().getProcessModelVersion().getVersionName();
        }

        IRichDialog rd = entry.getRichDialog();
        String panelClass = getPanelClass(rd);
        
        String namespaceSection = entry.getProject().getProjSection() + ".2." + entry.getRdNamespaceSection() + ".";
        row.setColumnValue("RDNamespace", namespaceSection + " " + rd.getNamespace());
        row.setColumnValue("RDId", namespaceSection + entry.getRdSection() + ". " +  getDialogName(entry, rd));
        row.setColumnValue("RichDialogPicture", entry.getRichDialogImage()); // will only be output if != null
        row.setColumnValue("RDPanelClass", panelClass);

        // Writes the interface of a rich dialog to the row.
        writeRichDialogInterfaceToRow(row, rd);

        // Write the RD Process data into the row.
        if (entry.getProcess() != null)
        {
          ProcessDataSetEventAdapter.writeProcessInfoToRow(row, entry.getProject().getIvyProject(), entry
                  .getProcessImage(highQuality), entry.getProcess(), entry.getProcessElement(), null, null, null, entry.getProcessId());
        }

        // Write the RD DataClass data into the row.
        if (entry.getDataClass() != null)
        {
          DataClassDataSetEventAdapter.writeDataClassToRow(row, entry.getDataClass(), null, null, entry.getDataClassId(), "DataClassId");
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

      return true;
    }
    return false;
  }

  private String getPanelClass(IRichDialog rd)
  {
    if (UlcDialogUtil.isUlcDialog(rd))
    {
      String panelClassName = rd.getPanelClassName();
      return panelClassName.substring(panelClassName.lastIndexOf('.') + 1);
    }
    return "";
  }

  @SuppressWarnings("null")
  private String getDialogName(RichDialogReportDataEntry entry, IRichDialog rd)
  {
    String dialogName = rd.getSimpleName();
    ZObject supernode = entry.getProcessElement().getSupernodeOrNull();
    boolean isElementInsideComponent = supernode != null && supernode.isProcessNode() && !supernode.isRootProcessNode();
    if (isElementInsideComponent)
    {
      dialogName = supernode.getVisibleName();
    }
    return dialogName;
  }

  /**
   * Writes the Interface (StartMethods, Methods, Fired Events, Accepted Broadcasts) of a Rich Dialog to a
   * DataSetRow.
   * 
   * @param row
   * @param rd
   * @throws ScriptException
   */
  // TODO jst: Get the Interface Description
  private void writeRichDialogInterfaceToRow(IUpdatableDataSetRow row, IRichDialog rd) throws ScriptException
  {
    final String marker = "* ";
    final String descSeparator = "\n";

    // Start Methods
    StringBuffer startMethods = new StringBuffer();
    for (IRdMethodDesc startMethod : rd.getStartMethods())
    {
      startMethods.append(marker);
      startMethods.append(startMethod.getMethodSignature());
      if (startMethod.getDescription().length() > 0)
      {
        startMethods.append(descSeparator);
        startMethods.append(startMethod.getDescription());
      }
      startMethods.append("\n");
    }
    row.setColumnValue("RDStartMethods", startMethods.toString());

    // Methods
    StringBuffer methods = new StringBuffer();
    for (IRdMethodDesc method : rd.getMethods())
    {
      methods.append(marker);
      methods.append(method.getDisplayString());
      if (method.getDescription().length() > 0)
      {
        methods.append(descSeparator);
        methods.append(method.getDescription());
      }
      methods.append("\n");
    }
    row.setColumnValue("RDMethods", methods.toString());

    // Fired Events
    StringBuffer firedEvents = new StringBuffer();
    for (IRdFiredEventDesc fe : rd.getFiredEvents())
    {
      firedEvents.append(marker);
      firedEvents.append(fe.getDisplayString());
      if (fe.getDescription().length() > 0)
      {
        firedEvents.append(descSeparator);
        firedEvents.append(fe.getDescription());
      }
      firedEvents.append("\n");
    }
    row.setColumnValue("RDFiredEvents", firedEvents.toString());

    // Accepted Broadcasts
    StringBuffer acceptedBCs = new StringBuffer();
    for (IRdAcceptedBroadcastEventDesc bcEvent : rd.getAcceptedBroadcastEvents())
    {
      acceptedBCs.append(marker);
      acceptedBCs.append(bcEvent.getDisplayString());
      if (bcEvent.getDescription().length() > 0)
      {
        acceptedBCs.append(descSeparator);
        acceptedBCs.append(bcEvent.getDescription());
      }
      acceptedBCs.append("\n");
    }
    row.setColumnValue("RDAcceptedBCs", acceptedBCs.toString());
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    rdIt = null;
  }
}
