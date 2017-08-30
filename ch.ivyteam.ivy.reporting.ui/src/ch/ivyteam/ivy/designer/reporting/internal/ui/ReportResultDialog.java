package ch.ivyteam.ivy.designer.reporting.internal.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.swt.icons.IconFactory;

/**
 * Dialog that shows the results from a report run. Provides links to
 * the directory containing the resulting reports and direct links to 
 * open the reports.
 * 
 * @author mda
 * @since 30.11.2010
 */
public class ReportResultDialog extends Dialog
{
  /** the report config whose result we show */
  private ReportConfiguration fReportConfiguration;

  /**
   * Constructor
   * @param shell the workbench window to show the dialog in
   * @param reportConfiguration the report configuration whose result are shown in this dialog
   */
  public ReportResultDialog(Shell shell, ReportConfiguration reportConfiguration)
  {
    super(shell);
    
    fReportConfiguration = reportConfiguration;
  }
  
  /**
   * Creates the list of links with all created reports
   * @param parent where to add the list
   */
  private void createResultList(final Composite parent)
  {
    /* Show a link to every single report. */
    if (fReportConfiguration.getReportFormats().size() > 0)
    {
      final Label reportsLabel = new Label(parent, SWT.NONE);
      reportsLabel.setText("Reports:");
      GridData data = new GridData();
      data.verticalIndent = 10;
      data.horizontalSpan = 2;
      reportsLabel.setLayoutData(data);

      final String filePath = fReportConfiguration.getAbsoluteReportFileName();
      final String fileName = fReportConfiguration.getReportFileName();
      for (final String format : fReportConfiguration.getReportFormats())
      {
        Link formatLink = new Link(parent, SWT.NONE);
        formatLink.setText("<a>" + fileName + "." + format + "</a>");
        
        GridData linkData = new GridData(GridData.FILL_HORIZONTAL);
        linkData.horizontalIndent = 30;
        linkData.horizontalSpan = 2;
        formatLink.setLayoutData(linkData);
        formatLink.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDown(MouseEvent arg0)
            {
              Program.launch("file:///" + filePath + format);
            }
        });
      }
    }
  }

  /**
   * Creates the link to the result folder
   * @param parent where to add the link
   */
  private void createResultFolderLink(final Composite parent)
  {
    /* Show a link to the report root. */
    final String fsRoot = fReportConfiguration.getFileSystemRoot();
    Link link = new Link(parent, SWT.NONE);
    link.setText("Reports written to Folder: <a>" + fsRoot + "</a>");
    link.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseDown(MouseEvent arg0)
      {
        Program.launch("file:///" + fsRoot);
      }
    });
    GridData data = new GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    data.verticalIndent = 20;
    link.setLayoutData(data);
  }

  /**
   * Creates the title label 
   * @param parent where to add the label
   */
  private void createSuccessLabel(final Composite parent)
  {
    /* The success message */
    final Label successLabel = new Label(parent, SWT.NONE);
    successLabel.setText("Report generation was successful!");
    successLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
    GridData data = new GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    successLabel.setLayoutData(data);
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent)
  {
    setDefaultImage(IconFactory.get().getIvyProject16());
    
    Composite root = new Composite(parent, SWT.NONE);
    
    GridLayout layout = new GridLayout(2, false);
    layout.marginLeft = layout.marginRight = 20;
    layout.marginTop = layout.marginBottom = 10;
    root.setLayout(layout);

    createSuccessLabel(root);
    createResultFolderLink(root);
    createResultList(root);
    
    return root;
  }

  /**
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell)
  {
    super.configureShell(newShell);
    newShell.setText("Report Generator");
  }
  
  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent)
  {
    createButton(parent, CANCEL, "Close", true);
  }
}
