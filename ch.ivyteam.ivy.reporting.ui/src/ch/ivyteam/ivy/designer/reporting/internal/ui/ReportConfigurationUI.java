package ch.ivyteam.ivy.designer.reporting.internal.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.ivyteam.di.restricted.DiCore;
import ch.ivyteam.ivy.Advisor;
import ch.ivyteam.ivy.IvyConstants;
import ch.ivyteam.ivy.application.IApplicationConfigurationManager;
import ch.ivyteam.ivy.application.IProcessModel;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.components.ProcessKind;
import ch.ivyteam.ivy.designer.ide.DesignerIDEPlugin;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.project.IvyProjectNavigationUtil;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.ivy.reporting.restricted.config.ReportElementFilter;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.richdialog.config.IRichDialog;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;
import ch.ivyteam.ivy.scripting.dataclass.IProjectDataClassManager;
import ch.ivyteam.log.Logger;
import ch.ivyteam.swt.dialogs.SwtCommonDialogs;
import ch.ivyteam.swt.icons.IconFactory;
import ch.ivyteam.util.Pair;

/**
 * Provides a GUI for the configuration of the report.
 * 
 * This class also implements a workbench action delegate. The action proxy will be created by the workbench
 * and shown in the UI. When the user tries to use the action, this delegate will be created and execution
 * will be delegated to it.
 * @see IWorkbenchWindowActionDelegate
 * 
 * @author jst
 * @since 28.05.2009
 */
public class ReportConfigurationUI
{  
  private static final Logger LOGGER = Logger.getClassLogger(ReportConfigurationUI.class);

  /** The default report design. */
  private final String DEFAULT_DESIGN_PATH;

  /** The default directory to save reports. */
  private final String DEFAULT_REPORTS_PATH;

  /** The default folder where Corporate Identity logos are read from. */
  private final String DEFAULT_LOGO_PATH;

  /** The default folder to which reporting configurations is written. */
  private final String DEFAULT_CONFIG_PATH;
  
  
  /** The default maximal nesting depth that is reported. */
  private final int DEFAULT_DEPTH = Integer.MAX_VALUE;

  /** The character limit for the report name text field. */
  private final int REPORT_NAME_LIMIT = 100;

  /** The window */
  private IWorkbenchWindow window;

  /** Associate TreeItems from the GUI to Ivy Process Model. */
  private Map<TreeItem, IProcessModel> item2pm;

  /** Associate TreeItems from the GUI to Ivy Process. */
  private Map<TreeItem, IProcess> item2proc;

  /** Associate TreeItems from the GUI to Ivy Rich Dialog. */
  private Map<TreeItem, IRichDialog> item2rd;

  /** Associate TreeItems from the GUI to Ivy Data Class. */
  private Map<TreeItem, IDataClass> item2dc;

  /** The Report Configuration Dialog. */
  private Shell configDialog;

  /** The Text Widget with the report file name */
  private Text reportNameText;

  /** The list with the output format buttons */
  private List<Button> formatButtonList;

  /** The Text Widget with the report destination */
  private Text destinationText;

  /** The Text Widget with the path of the report design */
  private Text designPathText;

  /** The Text Widget with the corporate id title */
  private Text titleText;

  /** The Text Widget with the corporate id header */
  private Text headerText;

  /** The Text Widget with the corporate id footer */
  private Text footerText;

  /** The Text Widget with the corporate id logo path */
  private Text logoLocationText;

  /** The Tree Widget with the Projects, Processes, etc... */
  private Tree projectTree;

  /** The Text Widget with the maximal nesting depth */
  private Text depthText;

  public ReportConfigurationUI(IWorkbenchWindow _window)
  {
    window = _window;
    
    File designerInstallDir = Advisor.getAdvisor().getInstallationDirectory();
    DEFAULT_DESIGN_PATH = new File(designerInstallDir, "/reporting/designs/").getAbsolutePath();
    DEFAULT_REPORTS_PATH = new File(designerInstallDir, "/reporting/").getAbsolutePath();
    DEFAULT_LOGO_PATH = new File(designerInstallDir, "/reporting/logos/").getAbsolutePath();
    DEFAULT_CONFIG_PATH = new File(designerInstallDir, "/reporting/configurations/").getAbsolutePath();
  }

  /**
   * Show the Configuration Dialog for the Report.
   * @param reportConfiguration The initial configuration of the dialog.
   */
  void showConfigurationDialog(ReportConfiguration reportConfiguration)
  {
    configDialog = new Shell(window.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    configDialog.setText("Report Configuration");
    configDialog.setImage(ImageDescriptor.createFromImage(IconFactory.get().getIvyProject16()).createImage());
    configDialog.setLocation(300, 20);
    RowLayout layout = new RowLayout(SWT.VERTICAL);
    layout.justify = true;
    layout.wrap = false;
    layout.fill = true;
    layout.spacing = 10;
    layout.marginBottom = layout.marginTop = layout.marginLeft = layout.marginRight = 10;
    configDialog.setLayout(layout);

    /* Create the Groups/Composites that make up the UI. */
    createReportGroup();
    createCorporateIdGroup();
    createProjectsGroup();
    createButtonComposite();

    /* Load the last configurations. */
    if (reportConfiguration != null)
    {
      fillDialogFromReportConfiguration(reportConfiguration);
    }

    configDialog.pack();
    configDialog.open();
  }

  /**
   * Creates the button composites
   */
  private void createButtonComposite()
  {
    Composite buttonComposite = new Composite(configDialog, SWT.NONE);
    buttonComposite.setLayout(new GridLayout(2, false));
    
    createSaveLoadButtonComposite(buttonComposite);
    createOkCancelButtonComposite(buttonComposite);    
  }

  /**
   * Starts a new job that writes the actual Report with help of the Reporting Manager.
   * @param reportConfig
   */
  private void writeReport(final ReportConfiguration reportConfig)
  {
    Job reportingJob = new Job("Creating report..")
    {
        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
          try
          {
            IReportingManager reportingManager = DiCore.getGlobalInjector().getInstance(IReportingManager.class);
            reportingManager.runAndRenderReport(reportConfig, monitor);
          }
          catch (Exception ex)
          {
            showErrorDialog(ex);
            return Status.CANCEL_STATUS;
          }
          showConfirmDialog(reportConfig);

          return Status.OK_STATUS;
        }
      };
    reportingJob.setUser(true);
    reportingJob.schedule();
  }

  /**
   * Show an asynchronous ErrorDialog.
   * 
   * @param ex The Exception.
   */
  private void showErrorDialog(final Exception ex)
  {
    Display.getDefault().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_ERROR | SWT.OK);
        messageBox.setText("Report creation failed.\n" +
      		"Please check that all Projects are in a clean state and try again.");
        messageBox.setMessage(ex.getMessage());
        messageBox.open();
      }
    });
  }

  /**
   * Show an asynchronous Dialog that links where the Reports has been saved.
   * @param reportConfiguration
   */
  private void showConfirmDialog(final ReportConfiguration reportConfiguration)
  {
    Display.getDefault().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        ReportResultDialog dialog = new ReportResultDialog(window.getShell(), reportConfiguration);
        dialog.open();
      }
    });
  }

  /**
   * Returns a ReportElementFilter that contains all the elements that have been selected from the Report
   * Configuration Dialog.
   * 
   * @return A filter that contains only the selected elements.
   */
  private ReportElementFilter getSelectedElements()
  {
    ReportElementFilter filter = new ReportElementFilter();
    try
    {
      filter.add(DesignerIDEPlugin.getApplicationConfigurationManager().getApplications().get(0));
    }
    catch (PersistencyException ex)
    {
      SwtCommonDialogs.openErrorDialog((Composite) null, ex);
    }
    for (TreeItem projectItem : projectTree.getItems())
    {
      // projects
      if (projectItem.getChecked())
      {
        LOGGER.trace("Accepted project: " + projectItem.getText());

        IProcessModel pm = item2pm.get(projectItem);
        LOGGER.trace("Accepted ProcessModel: " + projectItem.getText());
        filter.add(pm);

        for (TreeItem elementTypeItem : projectItem.getItems())
        {
          if (elementTypeItem.getText().equalsIgnoreCase("Processes"))
          {
            getSelectedProcesses(filter, elementTypeItem);
          }
          else if (elementTypeItem.getText().equalsIgnoreCase("Rich Dialogs"))
          {
            getSelectedRichDialogs(filter, elementTypeItem);
          }
          else if (elementTypeItem.getText().equalsIgnoreCase("Data Classes"))
          {
            getSelectedDataClasses(filter, elementTypeItem);
          }
          else
          {
            LOGGER.error("Received illegal element type");
          }
        }
      }
    }
    return filter;
  }

  /**
   * Add all the selected processes to the given filter when given the 'Processes' TreeItem.
   * 
   * @param filter
   * @param processesItem
   */
  private void getSelectedProcesses(ReportElementFilter filter, TreeItem processesItem)
  {
    // Process Groups
    for (TreeItem procGroupItem : processesItem.getItems())
    {
      if (procGroupItem.getChecked())
      {
        // Processes
        for (TreeItem procItem : procGroupItem.getItems())
        {
          if (procItem.getChecked())
          {
            LOGGER.trace("Accepted Process: " + procItem.getText());
            filter.add(item2proc.get(procItem));
          }
        }
      }
    }
  }

  /**
   * Add all the selected rich dialogs to the given filter when given the 'Rich Dialogs' TreeItem.
   * 
   * @param filter
   * @param richdialogsItem
   */
  private void getSelectedRichDialogs(ReportElementFilter filter, TreeItem richdialogsItem)
  {
    // RD Namespaces
    for (TreeItem rdNamespaceItem : richdialogsItem.getItems())
    {
      if (rdNamespaceItem.getChecked())
      {
        // RDs
        for (TreeItem rdNameItem : rdNamespaceItem.getItems())
        {
          if (rdNameItem.getChecked())
          {
            LOGGER.trace("Accepted Rich Dialog: " + rdNameItem.getText());
            filter.add(item2rd.get(rdNameItem));
          }
        }
      }
    }
  }

  /**
   * Add all the selected data classes to the given filter when given the 'Data Classes' TreeItem.
   * 
   * @param filter
   * @param dataClassesItem
   */
  private void getSelectedDataClasses(ReportElementFilter filter, TreeItem dataClassesItem)
  {
    // Data Class Namespaces
    for (TreeItem dcNamespaceItem : dataClassesItem.getItems())
    {
      if (dcNamespaceItem.getChecked())
      {
        // Data Classes
        for (TreeItem dcNameItem : dcNamespaceItem.getItems())
        {
          if (dcNameItem.getChecked())
          {
            LOGGER.trace("Accepted Data Class: " + dcNameItem.getText());
            filter.add(item2dc.get(dcNameItem));
          }
        }
      }
    }
  }

  /**
   * Add project information to a tree widget.
   * @param tree The tree to add the Project Items.
   */
  private void fillProjectTree(Tree tree)
  {
    item2pm = new Hashtable<TreeItem, IProcessModel>();
    item2proc = new Hashtable<TreeItem, IProcess>();
    item2rd = new Hashtable<TreeItem, IRichDialog>();
    item2dc = new Hashtable<TreeItem, IDataClass>();

    try
    {
      IApplicationConfigurationManager acm = DesignerIDEPlugin.getDefault().getServer().getApplicationConfigurationManager();
        
      for (IProcessModel pm : acm.getApplications().get(0).getProcessModels())
      {
        /* ProcessModels */
        TreeItem pmItem = new TreeItem(tree, SWT.NONE);
        pmItem.setText(pm.getName());
        item2pm.put(pmItem, pm);
        for (IProcessModelVersion pmv : pm.getProcessModelVersions())
        {
          /* Projects */
          IIvyProject ivyProject = IvyProjectNavigationUtil.getIvyProject(pmv.getProject());

          /* Processes */
          getProcessTree(pmItem, ivyProject);

          /* Rich Dialogs */
          getRichDialogTree(pmItem, ivyProject);

          /* Data Classes */
          getDataClassTree(pmItem, ivyProject);

        }
      }
    }
    catch (PersistencyException ex)
    {
      LOGGER.error("Faild to access Project Information.", ex);
      throw new RuntimeException("Faild to access Project Information.", ex);
    }
    catch (ResourceDataModelException ex)
    {
      LOGGER.error("Faild to access Project Information.", ex);
      throw new RuntimeException("Faild to access Project Information.", ex);
    }
  }

  /**
   * Add the process groups and names to a tree widget.
   * @param parentItem
   * @param ivyProject
   * @throws ResourceDataModelException
   */
  private void getProcessTree(TreeItem parentItem, IIvyProject ivyProject) throws ResourceDataModelException
  {
    // Gets the Process Group-Name Hierarchy
    Map<String, List<Pair<String, IProcess>>> processGroup2Procs = new HashMap<String, List<Pair<String, IProcess>>>();
    for (IProcess process : ivyProject.getProcesses(null))
    {
      // Don't show Rich Dialog Processes
      ProcessKind kind = process.getModRootZObject().getProcessKind();
      if (kind != ProcessKind.RICH_DIALOG || kind != ProcessKind.HTML_DIALOG)
      {
        // Add a mapping from process group to name
        String processName = process.getStorage().getName();
        processName = processName.substring(0, processName.length() - 4);
        String processGroup = process.getProcessGroup();

        List<Pair<String, IProcess>> procs;
        if (processGroup2Procs.containsKey(processGroup))
        {
          procs = processGroup2Procs.get(processGroup);
          procs.add(new Pair<String, IProcess>(processName, process));
        }
        else
        {
          procs = new LinkedList<Pair<String, IProcess>>();
          procs.add(new Pair<String, IProcess>(processName, process));
          processGroup2Procs.put(processGroup, procs);
        }
      }
    }

    // Create the Process SubTree
    if (processGroup2Procs.size() > 0)
    {
      // Processes Node
      TreeItem processesItem = new TreeItem(parentItem, SWT.NONE);
      processesItem.setText("Processes");

      for (Map.Entry<String, List<Pair<String, IProcess>>> entry : processGroup2Procs.entrySet())
      {
        // Process Group Nodes
        TreeItem procGroupItem = new TreeItem(processesItem, SWT.NONE);
        procGroupItem.setText(entry.getKey());
        for (Pair<String, IProcess> procPair : entry.getValue())
        {
          // Processes
          TreeItem procNameItem = new TreeItem(procGroupItem, SWT.NONE);
          procNameItem.setText(procPair.getLeft());
          item2proc.put(procNameItem, procPair.getRight());
        }
      }
    }
  }

  /**
   * Add the rich dialog namespaces and names to a tree widget.
   * @param parentItem
   * @param ivyProject
   * @throws ResourceDataModelException
   */
  private void getRichDialogTree(TreeItem parentItem, IIvyProject ivyProject)
          throws ResourceDataModelException
  {
    // Gets the RichDialog Namespace-Name Hierarchy
    Map<String, List<Pair<String, IRichDialog>>> richDialogNS2RDs = new HashMap<String, List<Pair<String, IRichDialog>>>();
    for (IRichDialog richDialog : ivyProject.getProjectRichDialogManager().getRichDialogs(null))
    {
      // Filter the basic rich dialogs
      if (!richDialog.getId().startsWith("ch.ivyteam.ivy.richdialog.rdpanels.basic"))
      {
        // Add a mapping from namespace to name
        String rdName = richDialog.getSimpleName();
        String rdNameSpace = richDialog.getNamespace();
        List<Pair<String, IRichDialog>> rds;
        if (richDialogNS2RDs.containsKey(rdNameSpace))
        {
          rds = richDialogNS2RDs.get(rdNameSpace);
          rds.add(new Pair<String, IRichDialog>(rdName, richDialog));
        }
        else
        {
          rds = new LinkedList<Pair<String, IRichDialog>>();
          rds.add(new Pair<String, IRichDialog>(rdName, richDialog));
          richDialogNS2RDs.put(rdNameSpace, rds);
        }
      }
    }

    // Create the Rich Dialog SubTree
    if (richDialogNS2RDs.size() > 0)
    {
      // Rich Dialogs Node
      TreeItem richDialogsItem = new TreeItem(parentItem, SWT.NONE);
      richDialogsItem.setText("Rich Dialogs");

      for (Map.Entry<String, List<Pair<String, IRichDialog>>> entry : richDialogNS2RDs.entrySet())
      {
        // Namespace Nodes
        TreeItem rdNameSpaceItem = new TreeItem(richDialogsItem, SWT.NONE);
        rdNameSpaceItem.setText(entry.getKey());
        for (Pair<String, IRichDialog> rdPair : entry.getValue())
        {
          // Rich Dialogs
          TreeItem rdNameItem = new TreeItem(rdNameSpaceItem, SWT.NONE);
          rdNameItem.setText(rdPair.getLeft());
          item2rd.put(rdNameItem, rdPair.getRight());
        }
      }
    }
  }

  /**
   * Add the data class namespaces and names to a tree widget.
   * @param parentItem
   * @param ivyProject
   * @throws ResourceDataModelException
   */
  private void getDataClassTree(TreeItem parentItem, IIvyProject ivyProject)
          throws ResourceDataModelException
  {
    // Gets the DataClass Namespace-Name Hierarchy
    Map<String, List<Pair<String, IDataClass>>> dataClassNS2DCs = new HashMap<String, List<Pair<String, IDataClass>>>();
    IProjectDataClassManager dataClassManager = ivyProject.getProjectDataClassManager();
    for (IDataClass dataClass : ivyProject.getDataClasses(null))
    {
      // Filter RD Data classes
      IResource resource = dataClass.getResource();
      if (dataClassManager.isDataModelResource(resource)
              && resource.getProjectRelativePath().segment(0).equals(IvyConstants.DIRECTORY_DATACLASSES))
      {
        // Add a mapping from namespace to name
        String dcName = dataClass.getName();
        String dcNameSpace = dcName.substring(0, dcName.lastIndexOf("."));
        dcName = dcName.substring(dcName.lastIndexOf(".") + 1, dcName.length());
        List<Pair<String, IDataClass>> dcs;
        if (dataClassNS2DCs.containsKey(dcNameSpace))
        {
          dcs = dataClassNS2DCs.get(dcNameSpace);
          dcs.add(new Pair<String, IDataClass>(dcName, dataClass));
        }
        else
        {
          dcs = new LinkedList<Pair<String, IDataClass>>();
          dcs.add(new Pair<String, IDataClass>(dcName, dataClass));
          dataClassNS2DCs.put(dcNameSpace, dcs);
        }
      }
    }

    // Create the Data classes SubTree
    if (dataClassNS2DCs.size() > 0)
    {
      // Data class Node
      TreeItem dataClassesItem = new TreeItem(parentItem, SWT.NONE);
      dataClassesItem.setText("Data Classes");

      for (Map.Entry<String, List<Pair<String, IDataClass>>> entry : dataClassNS2DCs.entrySet())
      {
        // Namespace Nodes
        TreeItem dcNameSpaceItem = new TreeItem(dataClassesItem, SWT.NONE);
        dcNameSpaceItem.setText(entry.getKey());
        for (Pair<String, IDataClass> dcPair : entry.getValue())
        {
          // Data Classes
          TreeItem dcNameItem = new TreeItem(dcNameSpaceItem, SWT.NONE);
          dcNameItem.setText(dcPair.getLeft());
          item2dc.put(dcNameItem, dcPair.getRight());
        }
      }
    }
  }

  /**
   * Set the tree to it's initial state. Check all items and expand all items up to the Process-, RD- and Data
   * class- Elements.
   * @param item The item to expand/check.
   * @param expand Should the item be expanded?
   */
  private void checkExpandTree(TreeItem item, boolean expand)
  {
    item.setChecked(true);

    if (item.getText().equals("Processes")
            || item.getText().equals("Rich Dialogs")
            || item.getText().equals("Data Classes"))
    {
      expand = false;
    }
    item.setExpanded(expand);

    for (TreeItem child : item.getItems())
    {
      checkExpandTree(child, expand);
    }
  }

  /**
   * Create the group that contains the format check buttons.
   */
  private void createReportGroup()
  {
    final Group groupReport = new Group(configDialog, SWT.NONE);
    groupReport.setLayout(new GridLayout(3, false));
    groupReport.setText("Report");

    Label labelName = new Label(groupReport, SWT.NONE);
    labelName.setText("Name:");
    
    reportNameText = new Text(groupReport, SWT.SINGLE | SWT.BORDER);
    reportNameText.setText("Name");
    reportNameText.setToolTipText("The name of your report file, without the trailing file extension (e.g. '.pdf').");
    reportNameText.setTextLimit(REPORT_NAME_LIMIT);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    reportNameText.setLayoutData(data);
    
    Label labelFormat = new Label(groupReport, SWT.NONE);
    labelFormat.setText("Output Format: ");
    
    Composite compositeFormat = new Composite(groupReport, SWT.NONE);
    compositeFormat.setLayoutData(data);
    compositeFormat.setLayout(new GridLayout(3, false));
    
    formatButtonList = new LinkedList<Button>();
    final Button htmlButton = new Button(compositeFormat, SWT.CHECK);
    htmlButton.setText("HTML");
    htmlButton.setData("html");
    htmlButton.setToolTipText("Use HTML for online documentation");
    htmlButton.setSelection(true);
    formatButtonList.add(htmlButton);
    
    final Button pdfButton = new Button(compositeFormat, SWT.CHECK);
    pdfButton.setText("PDF");
    pdfButton.setData("pdf");
    pdfButton.setToolTipText("Use PDF format for printing");
    pdfButton.setSelection(true);
    formatButtonList.add(pdfButton);
    
    final Button docButton = new Button(compositeFormat, SWT.CHECK);
    docButton.setText("Word");
    docButton.setData("doc");
    docButton.setToolTipText("Use Word doc format for printing");
    docButton.setSelection(true);
    formatButtonList.add(docButton);

    createSaveReportPart(groupReport);    
    createReportDesignPart(groupReport);
    
    groupReport.pack();
  }

  /**
   * Creates the save label, text and browse button to set the dir where to save the reports(s)
   * @param parent
   */
  private void createSaveReportPart(final Group parent)
  {
    Label saveLabel = new Label(parent, SWT.NONE); 
    saveLabel.setText("Save to Folder: ");

    destinationText = new Text(parent, SWT.SINGLE | SWT.BORDER);
    destinationText.setText(DEFAULT_REPORTS_PATH);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    destinationText.setLayoutData(data);
    destinationText.setToolTipText("The destination folder, where the reports will be written to.");

    Button directoryButton = new Button(parent, SWT.PUSH);
    directoryButton.setText("Choose...");
    directoryButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        DirectoryDialog dirDialog = new DirectoryDialog(configDialog);
        String filterPath = null;
        if (destinationText.getText() != null && new File(destinationText.getText()).exists())
        {
          filterPath = destinationText.getText();
        }
        else
        {
          filterPath = DEFAULT_REPORTS_PATH;
        }
        dirDialog.setFilterPath(filterPath);
        dirDialog.setText("Choose a report direcotry...");
        dirDialog.setMessage("Choose a directory where the generated report should be stored.");
        if (dirDialog.open() != null)
        {
          destinationText.setText(new Path(dirDialog.getFilterPath()).addTrailingSeparator().toOSString());
        }
      }
    });
  }

  /**
   * Create a group that allows the selection of Report Designs.
   * @param parent 
   */
  private void createReportDesignPart(Group parent)
  {
    Label designLabel = new Label(parent, SWT.NONE);
    designLabel.setText("Report Template: ");

    designPathText = new Text(parent, SWT.SINGLE | SWT.BORDER);    
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    designPathText.setLayoutData(data);
    designPathText.setText(DEFAULT_DESIGN_PATH+File.separatorChar+"BusinessReport.rptdesign");
    designPathText.setToolTipText("The Birt Report Design File (*.rptdesign) which describes the structure and contents of the report.");

    Button designButton = new Button(parent, SWT.PUSH);
    designButton.setText("Choose...");
    designButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog fileDialog = new FileDialog(configDialog, SWT.OPEN);
        fileDialog.setText("Choose the report design file.");
        String filterPath = DEFAULT_DESIGN_PATH;
        if (designPathText.getText() != null && new File(designPathText.getText()).exists())
        {
          filterPath = designPathText.getText();
        }
        fileDialog.setFilterPath(filterPath);
        fileDialog.setFilterExtensions(new String[] {"*.rptdesign", "*.*"});
        fileDialog.setFilterNames(new String[] {"Birt Report Design Files (*.rptdesign)", "All Files (*.*)"});
        if (fileDialog.open() != null)
        {
          designPathText.setText(new Path(fileDialog.getFilterPath() + "/" + fileDialog.getFileName()).toOSString());
        }
      }
    });
  }

  /**
   * Creates the group widget that contains the widgets to modify the Corporate Identity.
   */
  private void createCorporateIdGroup()
  {
    Group groupCorporateId = new Group(configDialog, SWT.NONE);
    groupCorporateId.setLayout(new GridLayout(3, false));
    groupCorporateId.setText("Corporate Identity");
    
    final Label titleLabel = new Label(groupCorporateId, SWT.NONE);
    titleLabel.setText("Title:");
    titleText = new Text(groupCorporateId, SWT.SINGLE | SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    titleText.setLayoutData(data);
    
    final Label headerLabel = new Label(groupCorporateId, SWT.NONE);
    headerLabel.setText("Header:");
    headerText = new Text(groupCorporateId, SWT.SINGLE | SWT.BORDER);
    headerText.setLayoutData(data);
    
    final Label footerLabel = new Label(groupCorporateId, SWT.NONE);
    footerLabel.setText("Footer:");
    footerText = new Text(groupCorporateId, SWT.SINGLE | SWT.BORDER);
    footerText.setLayoutData(data);
    
    createCiLogoComposite(groupCorporateId);
    
    groupCorporateId.pack();
  }

  /**
   * Create a label, text and button to choose which logo should be picked.
   * 
   * @param groupCorporateId
   */
  private void createCiLogoComposite(final Group groupCorporateId)
  {
    final Label logoLabel = new Label(groupCorporateId, SWT.NONE);
    logoLabel.setText("Logo:");
    logoLocationText = new Text(groupCorporateId, SWT.SINGLE | SWT.BORDER);
    logoLocationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Button fileButton = new Button(groupCorporateId, SWT.PUSH);
    fileButton.setText("Choose...");
    fileButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog fileDialog = new FileDialog(configDialog, SWT.OPEN);
        fileDialog.setText("Choose the report Corporate Identity Logo");
        String filterPath = null;
        if (logoLocationText.getText() != null && new File(logoLocationText.getText()).exists())
        {
          filterPath = logoLocationText.getText();
        }
        else
        {
          filterPath = DEFAULT_LOGO_PATH;

        }
        fileDialog.setFilterPath(filterPath);
        fileDialog.setFilterExtensions(new String[] {"*.*", "*.png", "*.jp*g", "*.gif"});
        fileDialog.setFilterNames(new String[] {"All Files (*.*)", "PNG Images (*.png)",
            "JPEG Images (*.jpg)", "GIF Images (*.gif)"});
        if (fileDialog.open() != null)
        {
          logoLocationText.setText(new Path(fileDialog.getFilterPath() + "/" + fileDialog.getFileName())
                  .toOSString());
        }
      }
    });
  }

  /**
   * Create Group Widget containing the Projects
   */
  private void createProjectsGroup()
  {
    Group groupProjects = new Group(configDialog, SWT.NONE);
    groupProjects.setLayout(new GridLayout(2, false));
    groupProjects.setText("Projects");
    createProjectTree(groupProjects);
    
    final Label lableDepth = new Label(groupProjects, SWT.NONE);
    lableDepth.setText("Maximum nesting depth in report: ");

    depthText = new Text(groupProjects, SWT.BORDER | SWT.SINGLE);
    depthText.setToolTipText("Hierarchy level up to which project elements are considered for the report. If empty, all elements will be considered.");
    depthText.addVerifyListener(new VerifyListener()
    {
        @Override
        public void verifyText(VerifyEvent e)
        {
          String old, start, end, newDepth;
          old = depthText.getText();
          start = old.substring(0, e.start);
          end = old.substring(e.end, old.length());
          newDepth = start + e.text + end;
          if (!newDepth.matches("\\p{Digit}*"))
          {
            e.doit = false;
          }
        }
    });
    
    groupProjects.pack();
  }

  /**
   * Creates a Tree widget which allows to select Projects, Processes, RDs and Data classes.
   * 
   * @param groupProjects
   */
  private void createProjectTree(Group groupProjects)
  {
    projectTree = new Tree(groupProjects, SWT.BORDER | SWT.CHECK);
    fillProjectTree(projectTree);
    for (TreeItem item : projectTree.getItems())
    {
      checkExpandTree(item, true);
    }
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    data.heightHint = 200;
    projectTree.setLayoutData(data);

    /*
     * Automatically check subitems if an item is checked and (un)check/(un)gray the ancestors of the item.
     */
    projectTree.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        if (e.detail == SWT.CHECK)
        {
          TreeItem item = (TreeItem) e.item;
          boolean checked = item.getChecked();
          checkChildren(item, checked);
          checkAncestors(item.getParentItem(), checked, false);
        }
      }

      /**
       * Ensures that when the checked state of an item changes, its ancestors in the tree are either
       * checked, grayed, or unchecked to match the new state of their children.
       */
      private void checkAncestors(TreeItem item, boolean checked, boolean grayed)
      {
        if (item == null)
          return;
        if (grayed)
        {
          checked = true;
        }
        else
        {
          for (TreeItem child : item.getItems())
          {
            if (child.getGrayed() || child.getChecked() != checked)
            {
              checked = grayed = true;
              break;
            }
          }
        }
        item.setChecked(checked);
        item.setGrayed(grayed);
        checkAncestors(item.getParentItem(), checked, grayed);
      }

      /**
       * Recursively checks or unchecks the item and all child items. All items are ungrayed.
       */
      private void checkChildren(TreeItem item, boolean checked)
      {
        item.setGrayed(false);
        item.setChecked(checked);
        for (TreeItem child : item.getItems())
        {
          checkChildren(child, checked);
        }
      }
    });
  }

  /**
   * Create a composite that contains the buttons to create, save, load and cancel.
   * @param parent the parent composite
   */
  private void createSaveLoadButtonComposite(Composite parent)
  {
    Composite buttonsComposite = new Composite(parent, SWT.NONE);
    GridData data = new GridData();
    data.horizontalAlignment = SWT.LEFT;
    buttonsComposite.setLayoutData(data);
    RowLayout compositeLayout = new RowLayout(SWT.HORIZONTAL);
    buttonsComposite.setLayout(compositeLayout);

    /* The Load Configuration button */
    createLoadConfigurationButton(buttonsComposite);

    /* The Save Configuration button */
    createSaveConfigurationButton(buttonsComposite);
  }
  
  /**
   * Create a composite that contains the buttons to create, save, load and cancel.
   * @param parent the parent composite
   */
  private void createOkCancelButtonComposite(Composite parent)
  {
    Composite buttonsComposite = new Composite(parent, SWT.NONE);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalAlignment = SWT.RIGHT;
    buttonsComposite.setLayoutData(data);
    RowLayout compositeLayout = new RowLayout(SWT.HORIZONTAL);
    buttonsComposite.setLayout(compositeLayout);

    Button createReportButton = new Button(buttonsComposite, SWT.PUSH);
    createReportButton.setText("Create the Report...");
    createReportButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        StringBuffer errors = new StringBuffer();
        ReportConfiguration config = createAndValidateReportConfigurationFromDialog(errors);
        if (config != null && checkReportOverwrite(config))
        {
          writeReport(config);
          ReportingPreferences.saveReportingPreferences(config);
          configDialog.close();
        }
        else if (config == null && errors.toString().length() > 0)
        {
          MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_WARNING | SWT.OK);
          messageBox.setText("Report configuration faulty.");
          messageBox.setMessage(errors.toString());
          messageBox.open();
        }
      }
    });
    
    Button cancelButton = new Button(buttonsComposite, SWT.PUSH);
    cancelButton.setText("Cancel");
    cancelButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        configDialog.close();
      }
    });
  }

  /**
   * The Save Configuration Button.
   * @param buttonsComposite
   */
  private void createSaveConfigurationButton(Composite buttonsComposite)
  {
    Button saveConfigurationButton = new Button(buttonsComposite, SWT.PUSH);
    saveConfigurationButton.setText("Save the Configuration...");
    saveConfigurationButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog fileDialog = new FileDialog(configDialog, SWT.SAVE);
        fileDialog.setText("Choose where to save the Reporting Configuration.");
        fileDialog.setFilterPath(DEFAULT_CONFIG_PATH);
        fileDialog.setFileName(reportNameText.getText() + "Config");
        fileDialog.setFilterExtensions(new String[] {"*.rptconfig", "*.*"});
        fileDialog.setFilterNames(new String[] {"Report Configuration Files (*.rptconfig)",
            "All Files (*.*)"});
        if (fileDialog.open() != null)
        {
          LOGGER.debug("Saving Configuration...");
          String configFilePath = new Path(fileDialog.getFilterPath() + "/" + fileDialog.getFileName())
                  .toOSString();

          if (checkConfigOverwrite(configFilePath))
          {
            StringBuffer errors = new StringBuffer();
            ReportConfiguration reportConfig = createAndValidateReportConfigurationFromDialog(errors);
            if (reportConfig != null)
            {
              try
              {
                DiCore.getGlobalInjector().getInstance(IReportingManager.class).persistReportConfiguration(reportConfig,
                        configFilePath);
              }
              catch (ReportingException ex)
              {
                LOGGER.error("Error writing report configuration.", ex);
                MessageBox messageBox = new MessageBox(configDialog, SWT.ICON_ERROR | SWT.OK);
                messageBox.setText("Error writing report configuration.");
                messageBox.setMessage(ex.getMessage());
                messageBox.open();
                return;
              }
              LOGGER.debug("Report Configuration persisted to " + configFilePath);
            }
            else if (errors.toString().length() > 0)
            {
              MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_WARNING | SWT.OK);
              messageBox.setText("Could not create report configuration.");
              messageBox.setMessage(errors.toString());
              messageBox.open();
            }
          }
        }
        else
        {
          LOGGER.debug("Did not overwrite configuration.");
        }
      }
    });
  }

  /**
   * The Load Configuration Button.
   * @param buttonsComposite
   */
  private void createLoadConfigurationButton(Composite buttonsComposite)
  {
    Button loadConfigurationButton = new Button(buttonsComposite, SWT.PUSH);
    loadConfigurationButton.setText("Load a Configuration...");
    loadConfigurationButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        String configFilePath = null;
        FileDialog fileDialog = new FileDialog(configDialog, SWT.OPEN);
        fileDialog.setText("Choose the Reporting Configuration to load.");
        fileDialog.setFilterPath(DEFAULT_CONFIG_PATH);
        fileDialog.setFilterExtensions(new String[] {"*.rptconfig", "*.*"});
        fileDialog.setFilterNames(new String[] {"Report Configuration Files (*.rptconfig)",
            "All Files (*.*)"});
        if (fileDialog.open() != null)
        {
          configFilePath = new Path(fileDialog.getFilterPath() + "/" + fileDialog.getFileName())
                  .toOSString();
          try
          {
            ReportConfiguration reportConfig = DiCore.getGlobalInjector().getInstance(IReportingManager.class)
                    .loadReportConfiguration(configFilePath);
            fillDialogFromReportConfiguration(reportConfig);
          }
          catch (ReportingException ex)
          {
            LOGGER.error("Error loading report configuration.", ex);
            MessageBox messageBox = new MessageBox(configDialog, SWT.ICON_ERROR | SWT.OK);
            messageBox.setText("Error loading report configuration.");
            messageBox.setMessage(ex.getMessage());
            messageBox.open();
            return;
          }
          LOGGER.debug("Report Configuration loaded from " + configFilePath);
        }
      }
    });
  }

  /**
   * Creates a report configuration from the config dialog and validated whether the configuration is valid.
   * If not, it returns null.
   * 
   * @param errorMessageBuffer An empty StringBuffer that will be filled with the errors of the report
   *          configuration. Should remain empty if everything went well.
   * @return A new report configuration. Returns null if something went wrong.
   */
  private ReportConfiguration createAndValidateReportConfigurationFromDialog(StringBuffer errorMessageBuffer)
  {
    assert errorMessageBuffer != null : "Error messge buffer may not be null.";

    /* Check Report name */
    String reportName = reportNameText.getText();
    if (reportName.trim().length() == 0)
    {
      errorMessageBuffer.append("Report name is empty!");
    }

    /* Get a the list of formats. */
    List<String> formats = new LinkedList<String>();
    for (Button formatButton : formatButtonList)
    {
      if (formatButton.getSelection())
      {
        String formatName = (String) formatButton.getData();
        formats.add(formatName);
      }
    }
    if (formats.size() == 0)
    {
      errorMessageBuffer.append("No report format selected! Select at least one output format.\n");
    }

    /* Check Report Design */
    String designPath = designPathText.getText();
    File designFile = new File(designPath);
    if (!designFile.exists())
    {
      errorMessageBuffer.append("Desgin file does not exist.\n");
    }
    else if (!designFile.canRead())
    {
      errorMessageBuffer.append("Design file cannot be read.\n");
    }

    /* Create & validate a Filter */
    ReportElementFilter filter = getSelectedElements();
    if (filter.getApplications().size() == 0)
    {
      errorMessageBuffer.append("No application was selected! Select at least one application.\n");
    }
    int maxDepth = (depthText.getText().equals("")) ? DEFAULT_DEPTH
            : Integer.parseInt(depthText.getText());
    filter.setMaxNestingDepth(maxDepth);

    /* Get the correct report destination */
    String destination = new Path(destinationText.getText()).addTrailingSeparator().toOSString();
    File destinationFile = new File(destination);
    if (!destinationFile.isDirectory())
    {
      errorMessageBuffer.append("Destination folder is not a directory!\n");
    }
    else if (!destinationFile.canWrite())
    {
      errorMessageBuffer.append("Cannot write to destination folder!\n");
    }

    /* Check the logo if it exists */
    String logoLocation = logoLocationText.getText();
    if (!logoLocation.equals(""))
    {
      File logoFile = new File(logoLocation);
      if (!logoFile.exists())
      {
        errorMessageBuffer.append("Logo file does not exist.\n");
      }
      else if (!logoFile.canRead())
      {
        errorMessageBuffer.append("Logo file cannot be read.\n");
      }
    }

    String errorMessage = errorMessageBuffer.toString();
    if (errorMessage.length() > 0)
    {
      return null;
    }
    /* Create the report configuration */
    return new ReportConfiguration(formats, destination, reportName,
            designPath, filter, 
            logoLocation, titleText.getText(), headerText.getText(), footerText.getText());

  }

  /**
   * Map a Report Configuration to the GUI.
   * 
   * @param reportConfig
   */
  private void fillDialogFromReportConfiguration(ReportConfiguration reportConfig)
  {
    // Set the Design
    designPathText.setText(reportConfig.getReportDesign());

    // Set the text and button widgets
    destinationText.setText(reportConfig.getFileSystemRoot());
    reportNameText.setText(reportConfig.getReportFileName());
    logoLocationText.setText(reportConfig.getLogo());
    titleText.setText(reportConfig.getTitle());
    headerText.setText(reportConfig.getHeader());
    footerText.setText(reportConfig.getFooter());

    // Set the format buttons
    for (Button formatButton : formatButtonList)
    {
      formatButton.setSelection(false);
    }
    for (String format : reportConfig.getReportFormats())
    {
      for (Button formatButton : formatButtonList)
      {
        if (((String) formatButton.getData()).equalsIgnoreCase(format))
        {
          formatButton.setSelection(true);
        }
      }
    }
    configDialog.layout();
  }

  /**
   * If a report file will be overwritten, this will start a dialog that asks the user if he really wants to
   * overwrite those files. Reports are only to be created when this method returns true.
   * 
   * @param config The report configuration.
   * @return Can we proceed with the report generation?
   */
  private boolean checkReportOverwrite(ReportConfiguration config)
  {
    boolean overwrite = false;
    for (String format : config.getReportFormats())
    {
      File file = new File(config.getAbsoluteReportFileName() + format);
      if (file.exists())
      {
        overwrite = true;
      }
    }

    if (overwrite)
    {
      MessageBox reallyDialog = new MessageBox(configDialog, SWT.ICON_WARNING | SWT.YES | SWT.NO);
      reallyDialog.setText("Overwrite report files?");
      reallyDialog.setMessage("Some reports already exists.\n" + "Do you want to overwrite them?");
      if (reallyDialog.open() == SWT.NO)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * If a config file will be overwritten, this will start a dialog that asks the user if he really wants to
   * overwrite this file. The files are only to be created when this method returns true.
   * 
   * @param filePath The path to the file.
   * @return Can we proceed with the saving of the file?
   */
  private boolean checkConfigOverwrite(final String filePath)
  {
    File file = new File(filePath);
    if (file.exists())
    {
      MessageBox reallyDialog = new MessageBox(configDialog, SWT.ICON_WARNING | SWT.YES | SWT.NO);
      reallyDialog.setText("Overwrite config file?");
      reallyDialog.setMessage("This configuration already exists.\n" +
              "Do you want to overwrite it?");
      if (reallyDialog.open() == SWT.NO)
      {
        return false;
      }
    }
    return true;
  }
}
