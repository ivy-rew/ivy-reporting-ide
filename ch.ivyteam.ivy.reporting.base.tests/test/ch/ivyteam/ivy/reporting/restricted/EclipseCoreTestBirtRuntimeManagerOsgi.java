package ch.ivyteam.ivy.reporting.restricted;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.ivyteam.di.restricted.BaseDiTest;
import ch.ivyteam.ivy.reporting.internal.BirtRuntimeManagerOsgi;

import javax.inject.Inject;

/**
 * Tests the {@link IBirtRuntimeManager} instance.
 * @author jst
 * @since 17.07.2009
 */
public class EclipseCoreTestBirtRuntimeManagerOsgi extends BaseDiTest
{
  /** The BirtRuntimeTestHelper */
  @Inject @Rule
  public BirtRuntimeTestHelper testHelper;

  /** The BirtRuntimeManager to operate on. */
  @Inject
  private IBirtRuntimeManager birtRuntimeManager;

  /** The path to the default test report. */
  private String testReportDesign;
  
  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception
  {
    testReportDesign = EclipseCoreTestBirtEngineSimpleReportGeneration.extractFromBundle("/testResources/testReport.rptdesign");
    
    // TODO: this test must be run twice, once for server (external BIRT) and once for designer (OSGI BIRT).
    Assert.assertTrue("Runtime manager has wrong type (Designer environment assumed)", birtRuntimeManager instanceof BirtRuntimeManagerOsgi);
  }

  /**
   * Test method for {@link ch.ivyteam.ivy.reporting.internal.BirtRuntimeManagerOsgi#getName()}.
   */
  @Test
  public void testGetName()
  {
    Assert.assertEquals(birtRuntimeManager.getName(), "Birt Runtime Manager [OSGI]");
  }

  /**
   * Test method for
   * {@link ch.ivyteam.ivy.reporting.internal.BirtRuntimeManagerOsgi#start(org.eclipse.core.runtime.IProgressMonitor)} and
   * {@link ch.ivyteam.ivy.reporting.internal.BirtRuntimeManagerOsgi#stop(org.eclipse.core.runtime.IProgressMonitor)} and
   * {@link BirtRuntimeManagerOsgi#isStarted()}. 
   */
  @Test
  public void testStartStop()
  {
    Assert.assertNotNull(birtRuntimeManager.getBirtEngine(IReportEngine.class, this));
    Assert.assertNotNull(birtRuntimeManager.getBirtEngine(IDesignEngine.class, this));
    Assert.assertTrue(birtRuntimeManager.isStarted());

    birtRuntimeManager.stop(new NullProgressMonitor());
    Assert.assertNull(birtRuntimeManager.getBirtEngine(IReportEngine.class, this));
    Assert.assertNull(birtRuntimeManager.getBirtEngine(IDesignEngine.class, this));
    Assert.assertFalse(birtRuntimeManager.isStarted());

    birtRuntimeManager.start(new NullProgressMonitor());
    Assert.assertNotNull(birtRuntimeManager.getBirtEngine(IReportEngine.class, this));
    Assert.assertNotNull(birtRuntimeManager.getBirtEngine(IDesignEngine.class, this));
    Assert.assertTrue(birtRuntimeManager.isStarted());
    
    birtRuntimeManager.stop(new NullProgressMonitor());
  }

  /**
   * Test method for {@link BirtRuntimeManagerOsgi#getAvailableThemes}
   */
  @Test
  public void testGetAvailableThemes()
  {
    List<String> themes;
    try
    {
      themes = birtRuntimeManager.getAvailableThemes(testReportDesign);
      Assert.assertTrue("Test report should not have any themes", themes.isEmpty());
      
      // TODO: this does not REALLY test the getAvailableThemes() method! this needs to test a report with themes!
      // however, reviewer (kvg) has no clue, so this is consequently "left as an exercise" for somebody else
    }
    catch (ReportingException ex)
    {
      Assert.fail("Could not access test report themes");
    }

  }

  /**
   * Test method for {@link BirtRuntimeManagerOsgi#getReportEngine}
   */
  @Test
  public void testGetReportEngine()
  {
    IReportEngine reportEngine = birtRuntimeManager.getBirtEngine(IReportEngine.class, this);
    Assert.assertNotNull(reportEngine);
  }

  /**
   * Test method for {@link BirtRuntimeManagerOsgi#getDesignEngine}
   */
  @Test
  public void testGetDesignEngine()
  {
    IDesignEngine designEngine = birtRuntimeManager.getBirtEngine(IDesignEngine.class, this);
    Assert.assertNotNull(designEngine);
  }

  /**
   * Test method for {@link BirtRuntimeManagerOsgi#createSimpleReport}
   * @throws Exception 
   */
  @Test
  public void testCreateSimpleReport() throws Exception
  {
    List<String> formats = Arrays.asList("pdf", "doc", "html");
    File tempReportDir = new File(System.getProperty("java.io.tmpdir"), "BirtTestReports_"+System.nanoTime());
    FileUtils.forceMkdir(tempReportDir);
    String reportName = "testReport";

    try
    {
      // create reports
      birtRuntimeManager.createSimpleReport(testReportDesign, formats, tempReportDir, reportName, null);
      Collection<File> generatedFiles = FileUtils.listFiles(tempReportDir, new String[]{ "pdf", "doc", "html" }, false);
      Assert.assertEquals("3 Reports should have been created.", 3, generatedFiles.size());
      for (File report : generatedFiles)
      {
        Assert.assertTrue(report.exists());
        Assert.assertTrue(report.length() > 0);
      }
    }
    finally
    {
      // cleanup
      FileUtils.forceDelete(tempReportDir);
    }
  }
  
}
