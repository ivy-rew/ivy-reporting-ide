package ch.ivyteam.ivy.reporting.internal;

import javax.inject.Singleton;

import org.eclipse.core.runtime.Platform;

import ch.ivyteam.ivy.manager.restricted.AbstractManagerProvider;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;

/**
 * Provides a {@link IReportingManager} instance depending on whether the eclipse platform is running or not 
 * @author rwei
 * @since 05.09.2012
 */
@Singleton
public class ReportingManagerProvider extends AbstractManagerProvider<IReportingManager>
{

  @Override
  public Class<? extends IReportingManager> getImplementedBy()
  {
    if (Platform.isRunning() && BirtRuntimeManagerProvider.birtClassesAreAccessible())
    {
      return ReportingManager.class;
    }
    else
    {
      return DummyReportingManager.class;
    }
  }

}
