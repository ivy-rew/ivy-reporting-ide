package ch.ivyteam.ivy.reporting.internal;

import java.util.List;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import ch.ivyteam.ivy.manager.restricted.AbstractManager;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;

/**
 * Dummy implementation of a reporting manager. Used when no eclipse platform is available.
 * @author rwei
 * @since 05.09.2012
 */
@Singleton
public class DummyReportingManager extends AbstractManager implements IReportingManager
{

  @Override
  public String getName()
  {
    return "ReportingManager";
  }

  @Override
  public void persistReportConfiguration(ReportConfiguration reportConfig, String configPath)
          throws ReportingException
  {
    throw new IllegalStateException("Not implemented");
  }

  @Override
  public ReportConfiguration loadReportConfiguration(String configPath) throws ReportingException
  {
    throw new IllegalStateException("Not implemented");
  }

  @Override
  public void runAndRenderReport(ReportConfiguration reportConfig, IProgressMonitor monitor)
          throws ReportingException
  {
    throw new IllegalStateException("Not implemented");    
  }

  @Override
  public List<String> getAvailableThemes(String designPath) throws ReportingException
  {
    throw new IllegalStateException("Not implemented");
  }

  @Override
  public boolean isReady()
  {
    return false;
  }

  @Override
  protected void doStart(SubMonitor progress) throws Exception
  {
  }

  @Override
  protected void doStop(SubMonitor progress) throws Exception
  {
  }
}
