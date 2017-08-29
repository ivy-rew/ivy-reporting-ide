package ch.ivyteam.ivy.reporting.restricted;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import ch.ivyteam.ivy.application.ApplicationConfigurationManagerTestHelper;
import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.ivy.reporting.restricted.config.ReportElementFilter;
import ch.ivyteam.test.AbstractRuleTestHelper;

import javax.inject.Inject;

/**
 * Reporting test helper only works in OSGI (e.g. EclipseCore) tests environment.
 * @author jst
 * @since 24.07.2009
 */
@Singleton
public class ReportingTestHelper extends AbstractRuleTestHelper
{
  /** The ApplicationConfigurationManagerTestHelper */
  @Inject
  private ApplicationConfigurationManagerTestHelper applicationConfigurationManagerTestHelper;

  /** The BirtRuntimeManager */
  @Inject
  private IBirtRuntimeManager birtRuntimeManager;

  /** The sample application */
  private IApplication application;

  /** The test report design */
  private String testReportDesign;

  /**
   * Sets up the reporting environment
   * @throws Exception
   */
  @Override
  public void doSetUp() throws Exception
  {
    applicationConfigurationManagerTestHelper.setUp();
    applicationConfigurationManagerTestHelper.start();
    application = applicationConfigurationManagerTestHelper.createTestApplication();
    
    testReportDesign = EclipseCoreTestBirtEngineSimpleReportGeneration.extractFromBundle("/testResources/testReport.rptdesign");
    birtRuntimeManager.start(new NullProgressMonitor());
  }

  /**
   * Tears down the reporting environment
   * @throws Exception
   */
  @Override
  public void doTearDown() throws Exception
  {
    birtRuntimeManager.stop(null);
    applicationConfigurationManagerTestHelper.stop();
    applicationConfigurationManagerTestHelper.tearDown();

  }

  /**
   * Returns the birtRuntimeManager
   * @return the birtRuntimeManager
   */
  public IBirtRuntimeManager getBirtRuntimeManager()
  {
    return birtRuntimeManager;
  }

  /**
   * Returns the reportConfiguration
   * @return the reportConfiguration
   */
  public ReportConfiguration createReportConfiguration()
  {
    List<String> formats = Arrays.asList("pdf", "html", "doc");
    String reportName = "TestReport";
    String reportPath = new Path(System.getProperty("java.io.tmpdir")).addTrailingSeparator().toOSString();
    String reportDesign = testReportDesign;
    String logo = "Logo";
    String title = "Title";
    String header = "Header";
    String footer = "Footer";

    String reportNameWithTime = reportName + "-" + Long.toString(System.nanoTime());
    ReportElementFilter filter = new ReportElementFilter();
    filter.add(application);
    ReportConfiguration reportConfiguration = new ReportConfiguration(formats, reportPath,
            reportNameWithTime, reportDesign,
            filter, logo, title, header, footer);
    return reportConfiguration;
  }
}
