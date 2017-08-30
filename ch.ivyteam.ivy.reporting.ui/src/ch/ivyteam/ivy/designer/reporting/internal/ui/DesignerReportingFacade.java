package ch.ivyteam.ivy.designer.reporting.internal.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.ivyteam.di.restricted.DiCore;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;

/**
 * @author jst
 * @since 13.08.2009
 */
public class DesignerReportingFacade implements IWorkbenchWindowActionDelegate
{
  /** The window */
  private IWorkbenchWindow window;

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public void init(IWorkbenchWindow _window)
  {
    this.window = _window;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run(IAction action)
  {
    if (DiCore.getGlobalInjector().getInstance(IReportingManager.class).isReady())
    {
      ReportConfigurationUI reportConfigurationUI = new ReportConfigurationUI(window);
      ReportConfiguration reportConfiguration = ReportingPreferences.loadReportingPreferences();
      reportConfigurationUI.showConfigurationDialog(reportConfiguration);
    }
    else
    {
      MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_ERROR | SWT.OK);
      messageBox.setText("Cannot create report.");
      messageBox.setMessage("Birt Engine seems to be missing or not started!");
      messageBox.open();
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection)
  {
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  @Override
  public void dispose()
  {
  }

}
