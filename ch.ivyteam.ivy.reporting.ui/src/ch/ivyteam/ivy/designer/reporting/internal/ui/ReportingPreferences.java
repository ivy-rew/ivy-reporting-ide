package ch.ivyteam.ivy.designer.reporting.internal.ui;

import java.io.File;

import ch.ivyteam.di.restricted.DiCore;
import ch.ivyteam.ivy.designer.ide.DesignerIDEPlugin;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.log.Logger;

/**
 * Activator for the {@link ReportingPreferences}.
 * @author kvg
 * @since 06.05.2010
 */
public class ReportingPreferences 
{
  /** The name of the configuration that has been used the last. */
  private final static String LAST_REPORT_CONFIG_NAME = "lastReportConfiguration.xml";
  private final static Logger LOGGER = Logger.getPackageLogger(ReportingPreferences.class);
  
  /**
   * Save a Report Configuration to the workspace plugin preferences.
   * @param reportConfig The report configuration to save.
   */
  static void saveReportingPreferences(ReportConfiguration reportConfig)
  {
    String configFn = DesignerIDEPlugin.getDefault().getStateLocation().append(LAST_REPORT_CONFIG_NAME).toOSString();
    try
    {
      DiCore.getGlobalInjector().getInstance(IReportingManager.class).persistReportConfiguration(reportConfig, configFn);
      LOGGER.debug("Preferences written to: " + configFn);
    }
    catch (ReportingException ex)
    {
      LOGGER.warn("Could not save preferences.", ex);
    }
  }

  /**
   * Load the last report configuration from the workspace plugin preferences.
   * @return The report configuration of the last report. May be null.
   */
  static ReportConfiguration loadReportingPreferences()
  {
    String configFn = DesignerIDEPlugin.getDefault().getStateLocation().append(LAST_REPORT_CONFIG_NAME).toOSString();
    File configFile = new File(configFn);
    ReportConfiguration reportConfiguration = null;
    if (configFile.exists() && configFile.canRead())
    {
      LOGGER.debug("Loading Preferences from: " + configFn);
      try
      {
        reportConfiguration = DiCore.getGlobalInjector().getInstance(IReportingManager.class).loadReportConfiguration(configFn);
      }
      catch (ReportingException ex)
      {
        LOGGER.warn("Could not load preferences.", ex);
      }
    }
    return reportConfiguration;
  }
}
