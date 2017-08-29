package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.components.ZObject;
import ch.ivyteam.ivy.components.wrap.Tool;
import ch.ivyteam.ivy.datawrapper.DataWrapperFactory;
import ch.ivyteam.ivy.datawrapper.DataWrapperNode;
import ch.ivyteam.ivy.datawrapper.DataWrapperProcess;
import ch.ivyteam.ivy.datawrapper.INameInfoExtended;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProcessReportDataEntry;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.log.Logger;

/**
 * Scripted Data Set for Process Groups, Processes and Process Elements.
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
public class ProcessDataSetEventAdapter extends ReportingDataSetEventAdapter
{
  private static final Logger LOGGER = Logger.getClassLogger(ProcessDataSetEventAdapter.class);
  /** zClassId -> user friendly element type name (statically initialized) */
  private static Map<String, String> sElementTypeNames = new HashMap<String, String>();

  /** A iterator through the process report data entries. */
  private Iterator<ProcessReportDataEntry> processIterator;

  /** The VersionName of the parent project. */
  private String parentVersionName;

  static
  {
    sElementTypeNames.put("EMail", "E-Mail");
    sElementTypeNames.put("Join", "Join");
    sElementTypeNames.put("GridStep", "Script");
    sElementTypeNames.put("ProgramInterface", "Program");
    sElementTypeNames.put("Trigger", "Trigger");
    sElementTypeNames.put("RichDialog", "Rich Dialog");
    sElementTypeNames.put("InfoButton", "Note");
    sElementTypeNames.put("WSElement", "Web Service");
    sElementTypeNames.put("Split", "Split");
    sElementTypeNames.put("ProcessException", "Exception");
    sElementTypeNames.put("EmbeddedSub", "Embedded Sub Process");
    sElementTypeNames.put("TaskSwitch", "Task Switch");
    sElementTypeNames.put("TaskSwitchSimple", "Simple Task Switch");
    sElementTypeNames.put("Alternative", "Alternative");
    sElementTypeNames.put("StartRequest", "Request Start");
    sElementTypeNames.put("EndRequest", "End Page");
    sElementTypeNames.put("IntermediateEvent", "Intermediate Event");
    sElementTypeNames.put("CallAndWait", "Call & Wait");
    sElementTypeNames.put("StartEvent", "Event Start");
    sElementTypeNames.put("ProgramUserInterface", "External UI");
    sElementTypeNames.put("DBStep", "Database");
    sElementTypeNames.put("CallSub", "Call Sub");
    sElementTypeNames.put("RichDialogInitStart", "Start"); 
    sElementTypeNames.put("RichDialogProcessStart", "Event Start");
    sElementTypeNames.put("RichDialogProcessStep", "Script");
    sElementTypeNames.put("RichDialogMethodStart", "Method Start");
    sElementTypeNames.put("RichDialogBroadcastStart", "Broadcast Start");
    sElementTypeNames.put("RichDialogProcessEnd", "Process End");
    sElementTypeNames.put("RichDialogEnd", "Exit End");
    sElementTypeNames.put("RichDialogUiSync", "UI Update");
    sElementTypeNames.put("EndTask", "Process End");
    sElementTypeNames.put("StartWS", "WS Start");
    sElementTypeNames.put("EndWS", "WS End");
    sElementTypeNames.put("StartSub", "Sub Start");
    sElementTypeNames.put("EndSub", "Sub End");
  }
  
  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    processIterator = reportDataCollector.getProcessReportData().iterator();
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
    while (processIterator.hasNext())
    {
      try
      {
        ProcessReportDataEntry entry = processIterator.next();

        /* Get only the entries that match the parent. */
        String versionName = entry.getProject().getProcessModelVersion().getVersionName();
        if(parentVersionName.equals(versionName))
        {        
          // Add the Process data to the row
          ProcessDataSetEventAdapter.writeProcessInfoToRow(
                  row, 
                  entry.getProject().getIvyProject(), 
                  entry.getProcessImage(highQuality),
                  entry.getProcess(), 
                  entry.getProcessElement(), 
                  entry.getProject().getProjSection(),
                  entry.getProcessGroupSection(), 
                  entry.getProcessSection(),
                  entry.getId());
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
      catch (ResourceDataModelException ex)
      {
        ReportingManager.getLogger().error("Error getting Report Data.", ex);
        throw new RuntimeException("Error getting Report Data.", ex);
      }
    }
    return false;
  }

  /**
   * Sets Process columns of a row to the corresponding values for a given process and process elements.
   * 
   * @param row
   * @param proj
   * @param img
   * @param zObjectOfProcess
   * @param subnode
   * @param projSection
   * @param procSection
   * @param groupSection
   * @param id 
   * @throws ScriptException
   * @throws ResourceDataModelException
   */
  static void writeProcessInfoToRow(
          IUpdatableDataSetRow row,
          IIvyProject proj,
          byte[] img, 
          ZObject zObjectOfProcess, 
          ZObject subnode, 
          String projSection,
          String groupSection, 
          String procSection, 
          int id) 
  throws ScriptException, ResourceDataModelException
  {
    // The Process Group with section
    IProcess process = proj.getProjectProcessManager().getProcessOf(zObjectOfProcess);
    String groupName = process.getProcessGroup();
    String fullGroupSection = null;
    if (projSection != null && groupSection != null)
    {
      fullGroupSection = projSection + ".1." + groupSection;
      groupName = fullGroupSection + ". " + groupName;
    }
    row.setColumnValue("ProcessGroup", groupName);

    String name = getName(zObjectOfProcess);
    
    // Add section to ProcessName
    if (fullGroupSection != null && procSection != null)
    {
      name = fullGroupSection + "." + procSection + ". " + name;
    }
    row.setColumnValue("ProcessName", name);
    row.setColumnValue("ProcessKind", zObjectOfProcess.getProcessKind().toString());
    row.setColumnValue("ProcessPicture", img);

    /* Get the Inscription mask details. */
    DataWrapperProcess dataWrapperProcess = (DataWrapperProcess) DataWrapperFactory.createDataWrapper(zObjectOfProcess, proj);
    INameInfoExtended nameInfo = dataWrapperProcess.getNameInfoExtended();
    row.setColumnValue("ProcessDescription", nameInfo.getDescription());
    StringBuffer documents = new StringBuffer();
    Tool[] documentTable = nameInfo.getDocumentTable();
    for (int i = 0; i < documentTable.length; i++)
    {
      documents.append(documentTable[i].getToolName());
      documents.append(": ");
      documents.append("<a href=\"");
      documents.append(documentTable[i].getUrl());
      documents.append("\">");
      documents.append(documentTable[i].getUrl());
      documents.append("</a>");
      documents.append("\n");
    }
    row.setColumnValue("ProcessDocuments", documents.toString());
    row.setColumnValue("ProcessElementId", id);

    /* Write the Process Element Information into the row. */
    ProcessDataSetEventAdapter.writeProcessElementInfoToRow(row, proj, subnode);
  }

  private static String getName(ZObject zObjectOfProcess)
  {
    String name = zObjectOfProcess.getName();
    boolean isComponent = zObjectOfProcess.isProcessNode() && !zObjectOfProcess.isRootProcessNode();
    if (isComponent)
    { 
      // As Composites and Processes have their Name stored differently, this is required 
      // to retrieve the right name.
      name = zObjectOfProcess.getVisibleName();
    }
    return name;
  }

  /**
   * Write the information about a process element into a DataSetRow.
   * @param row
   * @param proj
   * @param procElement
   * @throws ScriptException
   */
  private static void writeProcessElementInfoToRow(IUpdatableDataSetRow row, IIvyProject proj, ZObject procElement)
  throws ScriptException
  {
    INameInfoExtended nameInfo;
    StringBuffer documents;
    Tool[] documentTable;
    
    /* Get the Process Elements */
    row.setColumnValue("ElementVisibleName", procElement.getVisibleName());
    row.setColumnValue("ElementType", getElementTypeName(procElement));

    /* Get Process Element Inscription mask details. */
    DataWrapperNode<?> elementDW = (DataWrapperNode<?>) DataWrapperFactory.createDataWrapper(procElement, proj);

    nameInfo = elementDW.getNameInfoExtended();
    row.setColumnValue("ElementDescription", nameInfo.getDescription());
    documents = new StringBuffer();
    documentTable = nameInfo.getDocumentTable();
    for (int i = 0; i < documentTable.length; i++)
    {
      documents.append(documentTable[i].getToolName());
      documents.append(": ");
      documents.append("<a href=\"");
      documents.append(documentTable[i].getUrl());
      documents.append("\">");
      documents.append(documentTable[i].getUrl());
      documents.append("</a>");
      documents.append("\n");
    }
    row.setColumnValue("ElementDocuments", documents.toString());

    /* Write the Process Element specific code into the row. */
    ProcessDataSetEventAdapter.writeProcessElementCodeToRow(row, elementDW);
  }

  /**
   * Transforms the given element class id to a user-friendly type name.
   * @param procElement class of an element
   * @return user friendly type name
   */
  private static String getElementTypeName(ZObject procElement)
  {
    String classId = procElement.getZClass().getClassId();
    if (procElement.isProcessNode())
    {
      classId = "EmbeddedSub"; 
    }
    String userFriendlyName = sElementTypeNames.get(classId);
    return (userFriendlyName == null) ? classId : userFriendlyName;
  }

  /**
   * Write the process element specific code into the DataSetRow. Note that currently the number of specific
   * rows for a Process Element is hard coded to MAX_ELEMENT_COLUMNS.
   * 
   * @param row
   * @param elementDataWrapper
   * @throws ScriptException
   */
  private static void writeProcessElementCodeToRow(IUpdatableDataSetRow row, DataWrapperNode<?> elementDataWrapper)
          throws ScriptException
  {
    // Process Elements may have MAX_ELEMENT_COLUMNS Columns of generic data, called ElementCol1, ...2, ...3, etc.
    final Integer MAX_ELEMENT_COLUMNS = 8;
    Map<String, String> infoMap = new LinkedHashMap<String, String>();
    elementDataWrapper.getCompleteInfo(infoMap);
    int columnIndex = 1;
    for (Map.Entry<String, String> entry : infoMap.entrySet())
    {
      String key = entry.getKey();
      String value = entry.getValue();
      if (StringUtils.isBlank(value))
      {
        continue;
      }
      row.setColumnValue("ElementCol" + columnIndex + "Name", key);
      row.setColumnValue("ElementCol" + columnIndex, value.trim());

      columnIndex++;
      if (columnIndex > MAX_ELEMENT_COLUMNS)
      {
        LOGGER.warn("Can not insert more element columns (ElementCol{0}) than there are provided in the report.", columnIndex);
        break;
      }
    }
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    processIterator = null;
  }
}
