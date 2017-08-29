package ch.ivyteam.ivy.reporting.internal;

import javax.inject.Singleton;

import org.eclipse.core.runtime.Platform;

import ch.ivyteam.ivy.manager.restricted.AbstractManagerProvider;
import ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager;

/**
 * The factory for creation and access of a BirtRuntime manager
 * 
 * @author jst
 * @since 16.07.2009
 */
@Singleton
public class BirtRuntimeManagerProvider extends AbstractManagerProvider<IBirtRuntimeManager>
{
  @Override
  public Class<? extends IBirtRuntimeManager> getImplementedBy()
  {
    if (Platform.isRunning() && birtClassesAreAccessible())
    {
      return BirtRuntimeManagerOsgi.class;
    }
    else
    {
      return BirtRuntimeManagerExternal.class;
    }
  }
  
  public static boolean birtClassesAreAccessible()
  {
    try
    {
      BirtRuntimeManagerProvider.class.getClassLoader().loadClass("org.eclipse.birt.report.engine.api.EngineConfig");
      return true;
    }
    catch (ClassNotFoundException ex)
    {
      return false;
    }
  }
}
