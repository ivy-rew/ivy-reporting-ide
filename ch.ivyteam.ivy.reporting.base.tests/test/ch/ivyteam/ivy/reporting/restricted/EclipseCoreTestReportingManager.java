package ch.ivyteam.ivy.reporting.restricted;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import ch.ivyteam.di.restricted.UseModules;
import ch.ivyteam.ivy.application.ApplicationConfigurationTestModule;
import ch.ivyteam.ivy.persistence.db.BaseSystemDatabaseTest;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;

/**
 * Tests the {@link ReportingManager} class.
 * @author jst
 * @since 17.08.2009
 */
@UseModules(ApplicationConfigurationTestModule.class)
public class EclipseCoreTestReportingManager extends BaseSystemDatabaseTest
{
  /** The ReportingTestHelper */
  @Inject @Rule
  public ReportingTestHelper reportingTestHelper;
  
  @Inject
  private IReportingManager reportingManager;

  /**
   * Test method for
   * {@link ReportingManager}
   * .
   */
  @Test
  public void testReportingManager()
  {
    assertNotNull("Could not instantiate ReportingManager", reportingManager);
  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#getName()}.
   */
  @Test
  public void testGetName()
  {
    assertTrue(reportingManager.getName().startsWith("Reporting Manager"));
  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#start(org.eclipse.core.runtime.IProgressMonitor)}
   * . and
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#stop(org.eclipse.core.runtime.IProgressMonitor)}
   */
  @Test
  public void testStartAndStop()
  {
    try
    {
      reportingManager.start(null);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail("Could not start reporting manager.");
    }
    finally
    {
      reportingManager.stop(null);
    }
  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#isReady()}.
   */
  @Test
  public void testIsReady()
  {
    try
    {
      assertFalse(reportingManager.isReady());
      reportingManager.start(null);
      assertTrue(reportingManager.isReady());
      reportingManager.stop(null);
      assertFalse(reportingManager.isReady());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail("Could not start reporting manager.");
    }
  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#persistReportConfiguration(ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration, java.lang.String)}
   * .
   */
  @Test
  public void testPersistReportConfiguration() 
  {
    reportingManager.start(null);
    ReportConfiguration reportConfiguration = reportingTestHelper.createReportConfiguration();
    String tmpFilePath = reportConfiguration.getAbsoluteReportFileName() + "rptconfig";
    try
    {
      reportingManager.persistReportConfiguration(reportConfiguration, tmpFilePath);
      File tmpFile = new File(tmpFilePath);
      assertTrue(tmpFile.exists());
      tmpFile.deleteOnExit();
    }
    catch (ReportingException ex)
    {
      ex.printStackTrace();
      fail("Could not persist Report Configuration.");
    }
    finally
    {
      reportingManager.stop(null);
    }
  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#loadReportConfiguration(java.lang.String)}
   * .
   * @throws ReportingException
   */
  @Test
  public void testLoadReportConfiguration() throws ReportingException
  {
    reportingManager.start(null);
    ReportConfiguration reportConfiguration = reportingTestHelper.createReportConfiguration();
    String tmpFilePath = reportConfiguration.getAbsoluteReportFileName() + "rptconfig";

    reportingManager.persistReportConfiguration(reportConfiguration, tmpFilePath);
    try
    {
      File tmpFile = new File(tmpFilePath);
      assertTrue(tmpFile.exists());
      ReportConfiguration loadedReportConfiguration = reportingManager.loadReportConfiguration(tmpFilePath);
      assertNotNull(loadedReportConfiguration);
      assertEquals(reportConfiguration.getReportFileName(), loadedReportConfiguration.getReportFileName());
      assertEquals(reportConfiguration.getFileSystemRoot(), loadedReportConfiguration.getFileSystemRoot());
      assertEquals(reportConfiguration.getTitle(), loadedReportConfiguration.getTitle());
      assertEquals(reportConfiguration.getHeader(), loadedReportConfiguration.getHeader());
      assertEquals(reportConfiguration.getFooter(), loadedReportConfiguration.getFooter());
      assertEquals(reportConfiguration.getLogo(), loadedReportConfiguration.getLogo());
      assertEquals(reportConfiguration.getReportDesign(), loadedReportConfiguration.getReportDesign());
      for (String format : reportConfiguration.getReportFormats())
      {
        loadedReportConfiguration.getReportFormats().contains(format);
      }
      tmpFile.deleteOnExit();
    }
    catch (ReportingException ex)
    {
      ex.printStackTrace();
      fail("Could not load Report Configuration.");
    }
    finally
    {
      reportingManager.stop(null);
    }

  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.ReportingManager#getAvailableThemes(java.lang.String)}
   * .
   */
  @Test
  public void testGetAvailableThemes()
  {
    String reportDesign = reportingTestHelper.createReportConfiguration().getReportDesign();
    List<String> themes;
    try
    {
      reportingManager.start(null);
      themes = reportingManager.getAvailableThemes(reportDesign);
      reportingManager.stop(null);
      assertNotNull(themes);
    }
    catch (ReportingException ex)
    {
      ex.printStackTrace();
      fail("Could not get Themes");
    }
  }
}
