package ch.ivyteam.ivy.reporting.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.ivyteam.eclipse.util.EclipseUtil;
import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.application.IProcessModel;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.components.ZClass;
import ch.ivyteam.ivy.components.ZObject;
import ch.ivyteam.ivy.datawrapper.DataWrapperFactory;
import ch.ivyteam.ivy.datawrapper.DataWrapperNode;
import ch.ivyteam.ivy.datawrapper.INameInfoExtended;
import ch.ivyteam.ivy.extension.IvyExtensionPointManagerFactory;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.project.IvyProjectNavigationUtil;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.DataClassReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProcessReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.RichDialogReportDataEntry;
import ch.ivyteam.ivy.reporting.restricted.IProcessImageGenerator;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.ivy.reporting.restricted.config.ReportElementFilter;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.richdialog.config.IProjectRichDialog;
import ch.ivyteam.ivy.richdialog.config.IRichDialog;
import ch.ivyteam.ivy.richdialog.config.RichDialogConstants;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;
import ch.ivyteam.log.Logger;
import ch.ivyteam.util.ImageUtils;

/**
 * This class is responsible for fetching all the data that is to be displayed in a report. This data will be
 * flattened such that every report element has is in one of the *ReportData lists.
 * @author jst
 */
public class ReportDataCollector
{
  private static final Logger LOGGER = Logger.getClassLogger(ReportDataCollector.class);

  /** The Report Data for the projects. */
  private List<ProjectReportDataEntry> projectReportData;

  /** The Report Data for the processes. */
  private List<ProcessReportDataEntry> processReportData;

  /** The Report Data for the rich dialogs. */
  private List<RichDialogReportDataEntry> richDialogReportData;

  /** The Report Data for the data classes. */
  private List<DataClassReportDataEntry> dataClassReportData;

  /** The Corporate Identity Logo (Can not be passed as parameter). */
  private byte[] ciLogo;

  /** The Process Element Types that should be ignored. */
  private final List<String> processElementsToIgnoreAlways = Arrays.asList(
          "MessageFlowInP-0n", "MessageFlowOutP-0n",
          "TextInP", "AnnotationInP-0n", 
          "PushTrueWFInG-01", "PushTrueWFOutG-01");

  /** The process elements that should be ignored, if not inscribed */
  private final List<String> processElementsToIgnoreIfNotInscribed = Arrays.asList(
          "RichDialogProcessEnd", "RichDialogEnd", "EndTask", "RichDialogUiSync");

  /** The next process identifier */
  private int processId = 0;

  /** The next data class identifier */
  private int dataClassId = 0;

  /**
   * Collects the data that is to be entered into the report. After this method the *ReportData lists should
   * be filled with report data entries to be entered into the report data sets.
   * 
   * @param reportConfig The report configuration.
   * @param progressMonitor 
   * @param needLowQuality 
   * @param needHighQuality 
   * @throws ReportingException
   */
  public void fetchReportData(ReportConfiguration reportConfig, boolean needHighQuality, boolean needLowQuality, IProgressMonitor progressMonitor)
  throws ReportingException
  {
    List<IIvyProject> projects = new ArrayList<IIvyProject>();

    // get the project data for the report
    ciLogo = getLogoImage(reportConfig.getLogo());
    projectReportData = new LinkedList<ProjectReportDataEntry>();
    processReportData = new LinkedList<ProcessReportDataEntry>();
    richDialogReportData = new LinkedList<RichDialogReportDataEntry>();
    dataClassReportData = new LinkedList<DataClassReportDataEntry>();
    
    try
    {
      ReportElementFilter filter = reportConfig.getFilter();
      for (IApplication app : filter.getApplications())
      {
        if (filter.accept(app))
        {
          for (IProcessModel pm : app.getProcessModels())
          {
            if (filter.accept(pm))
            {
              IProcessModelVersion pmv = pm.getReleasedProcessModelVersion();
              IProject project = pmv.getProject();
              IIvyProject ivyProject = IvyProjectNavigationUtil.getIvyProject(project);
              projects.add(ivyProject);
            }
          }
        }
      }
      progressMonitor = EclipseUtil.ensureProgressMonitor(progressMonitor);
      progressMonitor.beginTask("Fetching report data.", projects.size());
      Integer projNumber = 0;
      for (IIvyProject ivyProject : projects)
      {        
        projNumber++;
        
        fetchProjectReportData(ivyProject, projNumber, filter, needHighQuality, needLowQuality,
                new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
      }
    }
    catch (ResourceDataModelException ex)
    {
      LOGGER.error("Resource data model cannot be read.", ex);
      throw new ReportingException("Resource data model cannot be read.", ex);
    }
    catch (PersistencyException ex)
    {
      LOGGER.error("Persistency access failed.", ex);
      throw new ReportingException("Persistency access failed.", ex);
    }
    finally
    {
      progressMonitor.done();
    }
  }

  /**
   * Fetch project report data
   * @param ivyProject
   * @param projectNumber 
   * @param filter 
   * @param needHighQuality 
   * @param needLowQuality 
   * @param progressMonitor
   * @throws ResourceDataModelException 
   * @throws PersistencyException 
   * @throws ReportingException 
   */
  private void fetchProjectReportData(
          IIvyProject ivyProject, 
          Integer projectNumber, 
          ReportElementFilter filter, 
          boolean needHighQuality, 
          boolean needLowQuality,
          IProgressMonitor progressMonitor) 
  throws ResourceDataModelException, PersistencyException, ReportingException
  {
    checkCancel(progressMonitor);
    
    progressMonitor.beginTask("Project "+ivyProject.getName()+".", 3);
    try
    {
      IProcessModelVersion pmv = ivyProject.getProcessModelVersion();
      ProjectReportDataEntry projDE = new ProjectReportDataEntry(pmv.getApplication(), pmv.getProcessModel(), pmv, ivyProject, projectNumber.toString());
      this.projectReportData.add(projDE);
      LOGGER.trace("IvyProject: " + ivyProject.getName());
  
      /* Processes */
      fetchAllProcessEntries(filter, projDE, needHighQuality, needLowQuality, new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
  
      /* Rich Dialogs */
      fetchAllRichDialogEntries(filter, projDE, needHighQuality, needLowQuality, new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
  
      /* Data Classes */
      fetchDataClassEntries(filter, projDE, new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
    }
    finally
    {
      progressMonitor.done();
    }
    
  }

  /**
   * Checks if user has canceled report generation.
   * @param progressMonitor
   * @throws ReportingException if user has canceled
   */
  private void checkCancel(IProgressMonitor progressMonitor) throws ReportingException
  {
    if (progressMonitor.isCanceled())
    {
      throw new ReportingException("Report generation canceled by user");
    }
  }

  /**
   * Get the Process entries of the project that are accepted by the filter. Also create the Report Sections
   * for the Table of contents.
   * @param filter
   * @param projDE
   * @param needLowQuality 
   * @param needHighQuality 
   * @param progressMonitor 
   * @throws ResourceDataModelException
   * @throws ReportingException if canceled
   */
  private void fetchAllProcessEntries(ReportElementFilter filter, ProjectReportDataEntry projDE, boolean needHighQuality, boolean needLowQuality, IProgressMonitor progressMonitor)
          throws ResourceDataModelException, ReportingException
  {
    List<IProcess> processes = sortProcesses(projDE);

    // Get all the report data entries for processes
    Integer procGroupNumber = 0;
    Map<String, Integer> procGroup2groupSection = new HashMap<String, Integer>();
    Map<String, Integer> procGroup2procSection = new HashMap<String, Integer>();
    progressMonitor.beginTask("", processes.size());
    try
    {
      for (IProcess proc : processes)
      {
        checkCancel(progressMonitor);
        
        if (filter.accept(proc))
        {
          String procGroup = proc.getProcessGroup();
  
          // Compute the section number for this process
          Integer procSection = procGroup2procSection.get(procGroup);
          if (procSection == null)
          {
            procSection = Integer.valueOf(0);
          }
          procGroup2procSection.put(procGroup, ++procSection);
  
          // Compute the section number for this process group
          Integer procGroupSection = procGroup2groupSection.get(procGroup);
          if (procGroupSection == null)
          {
            procGroup2groupSection.put(procGroup, ++procGroupNumber);
            procGroupSection = procGroupNumber;
          }
          List<ProcessReportDataEntry> processEntries = fetchReportEntriesForProcess(projDE, 
                  proc.getModRootZObject(), filter.getMaxNestingDepth(), procGroupSection.toString(), 
                  procSection.toString(), needHighQuality, needLowQuality, 
                  new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
          processReportData.addAll(processEntries);
          LOGGER.trace("Process Name: " + proc.getModRootZObject().getName());
        }
        else
        {
          progressMonitor.worked(1);
        }
      }
    }
    finally
    {
      progressMonitor.done();
    }
  }

  /**
   * Sort the processes.
   * @param projDE
   * @return A List of IProcesses sorted by name.
   * @throws ResourceDataModelException
   */
  private List<IProcess> sortProcesses(ProjectReportDataEntry projDE) throws ResourceDataModelException
  {
    class IProcessComparator implements Comparator<IProcess>
    {
      /**
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare(IProcess proc1, IProcess proc2)
      {
        return proc1.getStorage().getName().compareToIgnoreCase(proc2.getStorage().getName());
      }
    }
    List<IProcess> processes = new LinkedList<IProcess>(projDE.getIvyProject().getProcesses(null));
    Collections.sort(processes, new IProcessComparator());
    return processes;
  }

  /**
   * Fetches the flattened Report data for a Process. Fills the processReportData List with
   * ProcessReportDataEntries to be used by the report data providers.
   * 
   * @param projDE
   * @param process
   * @param section
   * @param maxDepth
   * @param groupSection
   * @param needLowQuality 
   * @param needHighQuality 
   * @param progressMonitor 
   * @return A list of processReportDataEntries. One entrie per ProcessElement.
   */
  private List<ProcessReportDataEntry> fetchReportEntriesForProcess(
          ProjectReportDataEntry projDE, ZObject process, int maxDepth,
          String groupSection, String section, boolean needHighQuality, boolean needLowQuality, IProgressMonitor progressMonitor)
  {

    List<ProcessReportDataEntry> result = new LinkedList<ProcessReportDataEntry>();

    progressMonitor = EclipseUtil.ensureProgressMonitor(progressMonitor);
    progressMonitor.beginTask("Process "+process.getName(), 1+(needLowQuality?1:0)+(needHighQuality?1:0));
    progressMonitor.subTask("");
    try
    {
      // Get the process image
      IProcessImageGenerator processImageGenerator = getProcessImageGenerator();
      byte[] processImageHighQuality = null;
      byte[] processImageLowQuality = null;
      if (processImageGenerator != null)
      {
        if (needHighQuality)
        {
          processImageHighQuality = processImageGenerator.getScaledByteArrayProcessImage(projDE.getIvyProject(), process, true);
          progressMonitor.worked(1);
        }
        if (needLowQuality)
        {
          processImageLowQuality = processImageGenerator.getScaledByteArrayProcessImage(projDE.getIvyProject(), process, false);
          progressMonitor.worked(1);
        }
      }
  
      // Get the process elements
      Enumeration<ZObject> subnodes = process.getSubnodes();
  
      // Even if there are no process elements, add the process.
      if (!subnodes.hasMoreElements())
      {
        result.add(new ProcessReportDataEntry(projDE, section, groupSection, process, processImageHighQuality, processImageLowQuality, null, processId++));
      }
  
      Integer subSection = 0;
      for(ZObject subnode : getSortedSubProcessNodes(projDE, subnodes))
      {
        // Compose the data that goes into the report
        result.add(new ProcessReportDataEntry(projDE, section, groupSection, process, processImageHighQuality, processImageLowQuality, subnode, processId++));
  
        /* Handle Composites */
        if (subnode.isProcessNode() && maxDepth > 0)
        {
          String subSectionString = section + "." + (++subSection);
          result.addAll(fetchReportEntriesForProcess(projDE, subnode, maxDepth - 1, groupSection, subSectionString, needHighQuality, needLowQuality, null));
        }
      }
      
    }
    finally
    {
      progressMonitor.done();
    }
    
    return sortProcessReportDataEntries(result);
  }

  private IProcessImageGenerator getProcessImageGenerator()
  {
    try
    {
      List<IProcessImageGenerator> extensions = IvyExtensionPointManagerFactory.getIvyExtensionPointManager().getExtensionsSilent(IProcessImageGenerator.class);
      return extensions.get(0);
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Failed to resolve an "+IProcessImageGenerator.class.getSimpleName()+" implementation.", ex);
    }
  }

  private List<ZObject> getSortedSubProcessNodes(ProjectReportDataEntry projDE, Enumeration<ZObject> subnodes)
  {
    List<ZObject> reportableSubNodes = new ArrayList<ZObject>();
    while (subnodes.hasMoreElements())
    {
      ZObject subnode = subnodes.nextElement();
      if (shouldReport(subnode, projDE.getIvyProject()))
      {
        reportableSubNodes.add(subnode);
      }
    }
    Collections.sort(reportableSubNodes, new VisibleZObjectNameComparator());
    return reportableSubNodes;
  }

  /**
   * Should the given process element be reported
   * @param processElement
   * @param project 
   * @return true if it should be reported, otherwise false
   */
  private boolean shouldReport(ZObject processElement, IIvyProject project)
  {
    ZClass zClass = processElement.getZClass();
    String zClassId = zClass.getClassId();
    
    if (zClass.getArcType() != null || processElementsToIgnoreAlways.contains(zClassId))
    {
      // ignore arcs + inner elements
      return false;
    }
    if (processElementsToIgnoreIfNotInscribed.contains(zClassId))
    {
      if (StringUtils.isNotBlank(processElement.getVisibleName()))
      {
        return true;
      }
      DataWrapperNode<?> elementDW = (DataWrapperNode<?>) DataWrapperFactory.createDataWrapper(processElement, project);
      INameInfoExtended nameInfo = elementDW.getNameInfoExtended();
      if (StringUtils.isBlank(nameInfo.getDescription()))
      {
        return false;
      }
    }
    return true;
  }
  
  private static class VisibleZObjectNameComparator implements Comparator<ZObject>
  {
    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(ZObject o1, ZObject o2)
    {
      return getVisibleName(o1).compareToIgnoreCase(getVisibleName(o2));
    }
    
    /**
     * @return Visible name or an empty string if null.
     */
    private String getVisibleName(ZObject object)
    {
      String name = null;
      if (object != null)
      {
        name = object.getVisibleName();
      }
      if (name == null)
      {
        name = "";
      }
      return name;
    }
    
  }
  
  private List<ProcessReportDataEntry> sortProcessReportDataEntries(List<ProcessReportDataEntry> entries)
  {
    class ProcessReportDataEntryComparator implements Comparator<ProcessReportDataEntry>
    {
      private VisibleZObjectNameComparator processNameComparator = new VisibleZObjectNameComparator();
      
      /**
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare(ProcessReportDataEntry e1, ProcessReportDataEntry e2)
      {
        return processNameComparator.compare(e1.getProcessElement(), e2.getProcessElement());
      }
    }
    
    Collections.sort(entries, new ProcessReportDataEntryComparator());
    return entries;
  }
  
  /**
   * Get the Rich Dialog entries of the project that are accepted by the filter. Also create the Report
   * Sections for the Table of contents.
   * @param filter
   * @param projDE
   * @param needHighQuality 
   * @param needLowQuality 
   * @param progressMonitor 
   * @throws ResourceDataModelException
   * @throws ReportingException if canceled
   */
  private void fetchAllRichDialogEntries(ReportElementFilter filter, ProjectReportDataEntry projDE, boolean needHighQuality, boolean needLowQuality, IProgressMonitor progressMonitor)
          throws ResourceDataModelException, ReportingException
  {
    List<IRichDialog> richDialogs = getRichDialogsSortedById(projDE);

    // Get all the report data entries for data classes
    Integer rdNamespaceNumber = 0;
    Map<String, Integer> namespace2section = new HashMap<String, Integer>();
    Map<String, Integer> namespace2rdSection = new HashMap<String, Integer>();
    progressMonitor.beginTask("", richDialogs.size());
    try
    {
      for (IRichDialog richDialog : richDialogs)
      {
        checkCancel(progressMonitor);

        if (filter.accept(richDialog))
        {
          String namespace = richDialog.getNamespace();
          // Compute the section number for this rd
          Integer rdSection = namespace2rdSection.get(namespace);
          if (rdSection == null)
          {
            rdSection = Integer.valueOf(0);
          }
          namespace2rdSection.put(namespace, ++rdSection);
  
          // Compute the section number for this rd namespace
          Integer namespaceSection = namespace2section.get(namespace);
          if (namespaceSection == null)
          {
            namespace2section.put(namespace, ++rdNamespaceNumber);
            namespaceSection = rdNamespaceNumber;
          }
          List<RichDialogReportDataEntry> rdEntries = fetchReportEntriesForRichDialog(projDE, richDialog,
                  filter.getMaxNestingDepth(), rdSection.toString(), namespaceSection.toString(), needHighQuality, needLowQuality, new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
          
          richDialogReportData.addAll(rdEntries);
          
          LOGGER.trace("RichDialog Id: " + richDialog.getId());
        }
      }
    }
    finally
    {
      progressMonitor.done();
    }
  }
  
  private List<IRichDialog> getRichDialogsSortedById(ProjectReportDataEntry projDE) throws ResourceDataModelException
  {
    class RdIdComporator implements Comparator<IRichDialog>
    {
      @Override
      public int compare(IRichDialog rd1, IRichDialog rd2)
      {
        return rd1.getId().compareToIgnoreCase(rd2.getId());
      }
    }
    List<IRichDialog> richDialogs = new LinkedList<IRichDialog>(projDE.getIvyProject().getProjectRichDialogManager().getRichDialogs(null));
    Collections.sort(richDialogs, new RdIdComporator());
    return richDialogs;
  }

  /**
   * Fetches the flattened Report data for a RichDialog.
   * @param projectDataEntry
   * @param richDialog
   * @param rdSection
   * @param maxDepth
   * @param rdNamespaceSection
   * @param needHighQuality 
   * @param needLowQuality 
   * @param progressMonitor 
   * @return A list of rd entries to be reported. One entry per RD ProcessElement or per binary RD.
   */
  private List<RichDialogReportDataEntry> fetchReportEntriesForRichDialog(
          ProjectReportDataEntry projectDataEntry,
          IRichDialog richDialog, 
          int maxDepth, 
          String rdSection, 
          String rdNamespaceSection,
          boolean needHighQuality, 
          boolean needLowQuality, 
          IProgressMonitor progressMonitor)
  {
    List<RichDialogReportDataEntry> result = new LinkedList<RichDialogReportDataEntry>();
    progressMonitor.beginTask("Rich Dialog "+richDialog.getId(), 4);
    progressMonitor.subTask("");

    byte[] richDialogScreenshot = getRichDialogScreenshotBytes(richDialog);
    progressMonitor.worked(1);
    
    try
    {
      IProcess process = richDialog.getProcess(new SubProgressMonitor(progressMonitor, 1));
      if (process != null)
      {
        IDataClass dataClass = richDialog.getDataClass(new SubProgressMonitor(progressMonitor, 1));
        List<ProcessReportDataEntry> processReportEntries = fetchReportEntriesForProcess(
                projectDataEntry, 
                process.getModRootZObject(), 
                maxDepth, 
                rdNamespaceSection, 
                rdSection, 
                needHighQuality, 
                needLowQuality, 
                null);
        
        for (ProcessReportDataEntry entry : processReportEntries) 
        {
          result.add(new RichDialogReportDataEntry(
                  projectDataEntry, 
                  entry.getProcessSection(), 
                  entry.getProcessGroupSection(),
                  richDialog, 
                  richDialogScreenshot,
                  entry.getProcess(), 
                  entry.getProcessImage(true), 
                  entry.getProcessImage(false), 
                  entry.getProcessElement(), 
                  dataClass,
                  entry.getId(),
                  dataClassId++));
        }
      }
      else
      {
        // Binary Rich Dialog without Process
        result.add(new RichDialogReportDataEntry(
                projectDataEntry,
                rdSection,
                rdNamespaceSection,
                richDialog, 
                richDialogScreenshot,
                null, null, null, null, null, -1, -1));
      }
    }
    finally
    {
      progressMonitor.done();
    }
    return result;
  }

  /**
   * Gets the rich dialog's screenshot as array of bytes with PNG encoding. 
   * If no screenshot exists for the given RichDialog, then a default image will be returned.
   * @param richDialog
   * @return image byte array (PNG encoded)
   */
  private byte[] getRichDialogScreenshotBytes(IRichDialog richDialog)
  {
    byte[] richDialogScreenshot = null; 
    if (richDialog instanceof IProjectRichDialog)
    {
      IFolder rdFolder = (IFolder) ((IProjectRichDialog) richDialog).getResource();
      IFile screenshotFile = (IFile) rdFolder.findMember(RichDialogConstants.SCREENSHOT_FILE_NAME);
      if (screenshotFile != null)
      {
        InputStream is = null;
        try
        {
          is = screenshotFile.getContents();
          BufferedImage img = ImageIO.read(is);
          richDialogScreenshot = ImageUtils.bufferedImageToPngByteArray(img, 150);
          return richDialogScreenshot;
        }
        catch (Exception ex)
        {
          LOGGER.warn("Could not read screenshot of RichDialog {0} ({1})", ex, richDialog.getId(), screenshotFile.getFullPath());
          return null; // no image
        }
        finally 
        {
          IOUtils.closeQuietly(is);
        }
      }
    }
    return richDialogScreenshot;
  }

  /**
   * Get the Data Classes entries of the project that are accepted by the filter. Also create the Report
   * Sections for the Table of contents.
   * @param filter
   * @param projDE
   * @param progressMonitor 
   * @throws ResourceDataModelException
   * @throws ReportingException if canceled
   */
  private void fetchDataClassEntries(ReportElementFilter filter, ProjectReportDataEntry projDE, IProgressMonitor progressMonitor)
          throws ResourceDataModelException, ReportingException
  {
    List<IDataClass> dataClasses = sortDataClasses(projDE);

    // Get all Report Data entries for the data classes
    Integer dcNamespaceNumber = 0;
    Map<String, Integer> namespace2section = new HashMap<String, Integer>();
    Map<String, Integer> namespace2dcSection = new HashMap<String, Integer>();
    
    progressMonitor.beginTask("", dataClasses.size());
    try
    {
      for (IDataClass dataClass : dataClasses)
      {
        checkCancel(progressMonitor);
        
        if (filter.accept(dataClass))
        {
          progressMonitor.subTask("Data Class "+dataClass.getName());
          String namespace = dataClass.getNamespace();
          // Compute the section number for this data class
          Integer dcSection = namespace2dcSection.get(namespace);
          if (dcSection == null)
          {
            dcSection = Integer.valueOf(0);
          }
          namespace2dcSection.put(namespace, ++dcSection);
  
          // Compute the section number for this data class namespace
          Integer namespaceSection = namespace2section.get(namespace);
          if (namespaceSection == null)
          {
            namespace2section.put(namespace, ++dcNamespaceNumber);
            namespaceSection = dcNamespaceNumber;
          }
          dataClassReportData.add(new DataClassReportDataEntry(projDE, dataClass, dcSection.toString(),
                  namespaceSection.toString(), dataClassId++));
          LOGGER.trace("Data Class: " + dataClass.getName());
        }
        progressMonitor.worked(1);
      }
    }
    finally
    {
      progressMonitor.done();
    }
  }

  /**
   * Sort the data classes.
   * @param projDE
   * @return A List of IDataClasses sorted by name.
   * @throws ResourceDataModelException
   */
  private List<IDataClass> sortDataClasses(ProjectReportDataEntry projDE) throws ResourceDataModelException
  {
    class IDataClassComparator implements Comparator<IDataClass>
    {
      /**
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare(IDataClass dc1, IDataClass dc2)
      {
        return dc1.getName().compareToIgnoreCase(dc2.getName());
      }
    }
    List<IDataClass> dataClasses = new LinkedList<IDataClass>(projDE.getIvyProject().getDataClasses(null));
    Collections.sort(dataClasses, new IDataClassComparator());
    return dataClasses;
  }

  /**
   * Returns the projectReportData
   * @return the projectReportData
   */
  public List<ProjectReportDataEntry> getProjectReportData()
  {
    return projectReportData;
  }

  /**
   * Returns the processReportData
   * @return the processReportData
   */
  public List<ProcessReportDataEntry> getProcessReportData()
  {
    return processReportData;
  }

  /**
   * Returns the richDialogReportData
   * @return the richDialogReportData
   */
  public List<RichDialogReportDataEntry> getRichDialogReportData()
  {
    return richDialogReportData;
  }

  /**
   * Returns the dataClassReportData
   * @return the dataClassReportData
   */
  public List<DataClassReportDataEntry> getDataClassReportData()
  {
    return dataClassReportData;
  }

  /**
   * Returns the logo file for the CI (corporate identity)
   * @return the logo file or null if not set or accessible
   */
  public byte[] getCiLogo()
  {
    return ciLogo == null ? null : ciLogo.clone();
  }

  /**
   * Returns the byte representation of an image.
   * 
   * @param logoPath The path to the image
   * @return A byte array containing the Image or null.
   */
  private byte[] getLogoImage(String logoPath)
  {
    if (StringUtils.isBlank(logoPath))
    {
      return null;
    }
    
    File ciLogoFile = new File(logoPath);
    if (! ciLogoFile.exists() || ! ciLogoFile.isFile() || ! ciLogoFile.canRead())
    {
      LOGGER.info("Corporate ID logo file '" + logoPath + "' was not found/readable.");
      return null;
    }

    byte[] imageBytes = null;
    InputStream is = null;
    try
    {
      BufferedImage img = ImageIO.read(ciLogoFile);
      imageBytes = ImageUtils.bufferedImageToPngByteArray(img, 96);
    }
    catch (IOException ex)
    {
      LOGGER.warn("Corporate ID logo '" + logoPath + "' could not be read (image will be missing)", ex);
    }
    finally
    {
      IOUtils.closeQuietly(is);
    }
    return imageBytes;
  }

}
