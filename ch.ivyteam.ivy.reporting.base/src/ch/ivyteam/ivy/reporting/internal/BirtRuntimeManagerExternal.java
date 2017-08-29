package ch.ivyteam.ivy.reporting.internal;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.core.runtime.SubMonitor;

import ch.ivyteam.ivy.manager.restricted.AbstractManager;
import ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.log.Logger;

/**
 * Birt runtime manager for external Birt engine. This implementation should
 * be used on the Server where OSGI is not (yet) supported. See {@link BirtRuntimeManagerOsgi} 
 * for the Designer implementation.
 * @author kvg
 * @since 03.05.2010
 */
// TODO: needs implementation (is currently only a dummy to be used on the server)
@Singleton
public class BirtRuntimeManagerExternal extends AbstractManager implements IBirtRuntimeManager
{
  private static final Logger LOGGER = Logger.getClassLogger(BirtRuntimeManagerExternal.class);
  
  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#createSimpleReport(String, List, File, String, Map)
   */
  @Override
  public void createSimpleReport(String designPath, List<String> formats, File reportPath, String reportName, Map<String, Object> parameterMap)
  {
    LOGGER.info("Creating of simple reports on the server is not yet supported: "+reportPath+"/"+reportName+" [report generation skipped]");
    // throw new UnsupportedOperationException("This operation is not yet available on the ivy Server");
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#getAvailableThemes(java.lang.String)
   */
  @Override
  public List<String> getAvailableThemes(String designPath) throws ReportingException
  {
    return Collections.emptyList();
  }
  
  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#getDesignParameters(java.lang.String)
   */
  @Override
  public List<String> getDesignParameters(String designPath) throws ReportingException
  {
    return Collections.emptyList();
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#getName()
   */
  @Override
  public String getName()
  {
    return "Birt Runtime Manager [Disabled]"; //[Standalone BIRT engine]";
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#isStarted()
   */
  @Override
  public boolean isStarted()
  {
    return false;
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#start(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void doStart(SubMonitor monitor)
  {
    // TODO: implement
    // use BirtRuntime directory from Server project (should only be distributed with Server, NOT on designer where BIRT is integrated as Plugins)
    // start birt engines based on external directory (BIRT_HOME = <server install dir>/BirtRuntime/...
    // a LOT of things can be stripped from the default BirtRuntime distribution for our purposes, see e.g. http://www.birt-exchange.org/devshare/deploying-birt-reports/987-webinar-archive-deploying-birt-within-applications/
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager#stop(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void doStop(SubMonitor monitor)
  {
    // TODO: implement
    // stop external engine(s)
  }

  @Override
  public <T> T getBirtEngine(Class<T> birtEngineClass, Object someContextObject)
  {
    throw new IllegalStateException("Not implemented");
  }

}
