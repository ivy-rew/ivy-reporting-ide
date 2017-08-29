package ch.ivyteam.ivy.reporting.internal;

import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.ivyteam.di.restricted.UseModules;
import ch.ivyteam.ivy.application.ApplicationConfigurationTestModule;
import ch.ivyteam.ivy.application.ApplicationDummy;
import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.persistence.db.BaseSystemDatabaseTest;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.DataClassReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProcessReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.RichDialogReportDataEntry;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.ivy.reporting.restricted.ReportingTestHelper;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;

/**
 * Tests the {@link ReportDataCollector} class.
 * @author jst
 * @since 25.08.2009
 */
@UseModules(ApplicationConfigurationTestModule.class)
public class EclipseCoreTestReportDataCollector extends BaseSystemDatabaseTest
{

  /** The Reporting Test Helper */
  @Inject @Rule
  public ReportingTestHelper reportingTestHelper;
  
  /** The sample Application */
  IApplication application;
  
  /**
   * @throws Exception 
   */
  @Before
  public void setUp() throws Exception
  {
    application = new ApplicationDummy(-1, "My Application", "The Application that was created by me!", "jst");
  }

  /**
   * @throws Exception 
   */
  @After
  public void tearDown() throws Exception
  {
    application = null;
  }

  /**
   * Test method for {@link ch.ivyteam.ivy.reporting.internal.ReportDataCollector#fetchReportData(ReportConfiguration, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)}
   */
  @Test
  public void testFetchReportData()
  {
    ReportDataCollector reportDataCollector = new ReportDataCollector();
    ReportConfiguration reportConfiguration = reportingTestHelper.createReportConfiguration();
    reportConfiguration.getFilter().add(application);
    try
    {
      reportDataCollector.fetchReportData(reportConfiguration, true, true, null);
    }
    catch (ReportingException ex)
    {
      Assert.fail("Could not fetch Report Data: "+ ex.getMessage());
    }
    
    // FIXME: this test doesn't really test much. all of the getters below return empty lists.
    // what happens if they're not empty? how should the data look?
    
    List<ProjectReportDataEntry> projectReportData = reportDataCollector.getProjectReportData();
    Assert.assertNotNull(projectReportData);
    
    List<ProcessReportDataEntry> processReportData = reportDataCollector.getProcessReportData();
    Assert.assertNotNull(processReportData);
    
    List<RichDialogReportDataEntry> richDialogReportData = reportDataCollector.getRichDialogReportData();
    Assert.assertNotNull(richDialogReportData);
    
    List<DataClassReportDataEntry> dataClassReportData = reportDataCollector.getDataClassReportData();
    Assert.assertNotNull(dataClassReportData);
  }

  /**
   * @param projectReportData
   * @throws PersistencyException
   */
  @SuppressWarnings("unused")
  private void dumpApplicationNames(List<ProjectReportDataEntry> projectReportData)
          throws PersistencyException
  {
    System.out.println("Nr. of ProjectEntries: " + projectReportData.size());
    for (ProjectReportDataEntry projectDE : projectReportData)
    {
      System.out.println("Application: "+ projectDE.getApplication().getName());
      System.out.println("ProcessModel: "+ projectDE.getProcessModel().getName());
      System.out.println("ProcessModelVersion: "+ projectDE.getProcessModelVersion().getName());
    }
  }
}
