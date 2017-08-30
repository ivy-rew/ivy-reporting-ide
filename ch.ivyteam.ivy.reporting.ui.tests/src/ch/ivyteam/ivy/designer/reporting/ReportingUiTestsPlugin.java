package ch.ivyteam.ivy.designer.reporting;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ReportingUiTestsPlugin extends AbstractUIPlugin implements BundleActivator
{
  private static ReportingUiTestsPlugin instance;

  @Override
  public void start(BundleContext context) throws Exception
  {
    instance = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception
  {
    instance = null;
  }
  
  public static ReportingUiTestsPlugin getDefault()
  {
    return instance;
  }

}
