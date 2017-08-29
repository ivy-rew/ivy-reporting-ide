package ch.ivyteam.ivy.reporting.restricted;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.ivyteam.ivy.manager.IManager;
import ch.ivyteam.ivy.reporting.internal.ReportingManagerProvider;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;

import com.google.inject.ProvidedBy;

/**
 * An IReportingManager is responsible for creating Birt reports. It is also responsible for reading and
 * storing report configurations.
 * 
 * @author jst
 * @since 13.08.2009
 */
@ProvidedBy(ReportingManagerProvider.class)
public interface IReportingManager extends IManager
{
  /**
   * Persists a ReportConfiguration to a XML file using Jaxb.
   * 
   * @param reportConfig
   * @param configPath
   * @throws ReportingException
   */
  public abstract void persistReportConfiguration(ReportConfiguration reportConfig, String configPath)
          throws ReportingException;

  /**
   * Load a persisted ReportConfiguration.
   * 
   * @param configPath The path of the file to be loaded.
   * @return The loaded report configuration.
   * @throws ReportingException
   */
  public abstract ReportConfiguration loadReportConfiguration(String configPath) throws ReportingException;

  /**
   * This method does the actual report generation. First it fetches the Data, then opens the report and fills
   * it with data and then renders the report to the corresponding output formats.
   * 
   * @param reportConfig The Report configuration.
   * @param monitor A IProgressMonitor to inform the caller about the progress.
   * @throws ReportingException When canceled or the report could not be generated.
   */
  public abstract void runAndRenderReport(ReportConfiguration reportConfig, IProgressMonitor monitor)
          throws ReportingException;

  /**
   * Given a report design, this method will return a list of Themes which are available to this report
   * design.
   * 
   * @param designPath The path to the *.rptdesign
   * @return The available themes for this report design. Never null.
   * @throws ReportingException When the design file could not be parsed.
   */
  public abstract List<String> getAvailableThemes(String designPath) throws ReportingException;

  /**
   * Returns if the Reporting Manager can be used.
   * @return Can the reporting manager be used, or are the Birt Engines missing?
   */
  boolean isReady();

}