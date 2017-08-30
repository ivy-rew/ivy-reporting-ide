package ch.ivyteam.ivy.designer.reporting.internal.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import ch.ivyteam.di.restricted.DiCore;
import ch.ivyteam.eclipse.util.EclipsePlatformUtils;
import ch.ivyteam.io.FileUtil;
import ch.ivyteam.ivy.designer.ide.DesignerIDEPlugin;
import ch.ivyteam.ivy.designer.project.restricted.AbstractDesignerEclipseTest;
import ch.ivyteam.ivy.designer.reporting.ReportingUiTestsPlugin;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.ivy.reporting.restricted.config.ReportElementFilter;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.richdialog.config.IRichDialog;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;

/**
 * Tests the report creation of a whole project, namely the project TestReportProject in the folder testResources. 
 * @author mspa
 * @since 26.07.2011
 */
public class EclipseCoreTestReporting extends AbstractDesignerEclipseTest
{
  /** project name of test project used in tests */
  private static final String TEST_PROJECT_NAME = "TestReportProject";
  /** zip of project */
  private static final String TEST_PROJECT_ZIP = "testResources/"+TEST_PROJECT_NAME+".zip";
  /** name of the report generated with the technical design */
  private static final String RESULT_NAME_TECHNICAL = "TestReportResult_Technical";
  /** temporary folder for generated  */
  private File fTempReportGenerationDirectory;
  /** */
  private File fTempReportSourceDirectory;
  
  /**
   * Constructor<br>
   * Fetches projectManager and project files and calls super.
   */
  public EclipseCoreTestReporting()
  {
    super(ReportingUiTestsPlugin.getDefault().getBundle(), TEST_PROJECT_ZIP);
  }
  
  /**
   * @see Before
   */
  @Before
  public void setUpTestCase()
  {
    fTempReportGenerationDirectory = FileUtil.createTemporaryDirectoryWhichIsDeleteOnExit();
    fTempReportSourceDirectory = FileUtil.createTemporaryDirectoryWhichIsDeleteOnExit();
  }

  /**
   * This tests the creation of a report with the technical design file 
   * and compares the generated report to a correct report.
   * @throws Exception 
   */
  @Test
  public void testProjectReportCreation() throws Exception
  {
    //use technical report design
    ReportConfiguration reportConfiguration = createReportConfiguration("TechnicalReport.rptdesign", RESULT_NAME_TECHNICAL);
    IReportingManager reportingManager = DiCore.getGlobalInjector().getInstance(IReportingManager.class);
    
    reportingManager.start(null);
    reportingManager.runAndRenderReport(reportConfiguration, null);
    reportingManager.stop(null);     
    
    compareReports(RESULT_NAME_TECHNICAL);
  }

  /**
   * Compare the generated report with a correct report. 
   * @param reportName 
   * @throws IOException
   */
  private void compareReports(String reportName) throws IOException
  {    
    File expectedReportFile = EclipsePlatformUtils.extractBundleEntry(ReportingUiTestsPlugin.getDefault().getBundle(), "testResources/" + reportName + ".html");
    String contentExpectedReport = FileUtils.readFileToString(expectedReportFile, "UTF-8");
    contentExpectedReport = subStringReportContent(contentExpectedReport);
    contentExpectedReport = removeDynamicContentFromReport(contentExpectedReport);
   
    File actualReportFile = new File(fTempReportGenerationDirectory, reportName + ".html");
    String contentActualReport = FileUtils.readFileToString(actualReportFile, "UTF-8");
    contentActualReport = subStringReportContent(contentActualReport);
    contentActualReport = removeDynamicContentFromReport(contentActualReport);

    
    // use assertTrue instead of assertEquals otherwise the whole 1.5MB content is on the log
    // and results in an out of memory on the build system
//    assertTrue("Content of reports must be equals", contentExpectedReport.equals(contentActualReport));
    assertEquals("Content of reports must be equals", contentExpectedReport, contentActualReport);
  }

  private String removeDynamicContentFromReport(String reportContent)
  {
    return reportContent.replaceAll("_[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}", "");
  }

  /**
   * @param contentGeneratedReport
   * @return -
   */
  private String subStringReportContent(String contentGeneratedReport)
  {
    int start = contentGeneratedReport.indexOf("Table of Contents");
    if (start == -1)
    {
       return "Errow while executing Test Case. Could not find content 'Table of Contents', to substring the content.";
    }
    return contentGeneratedReport.substring(start);
  }
  
  /**
   * @param testReportName 
   * @param resultFileName 
   * @return report configuration with information where to store report, what design to use...
   * @throws ResourceDataModelException 
   * @throws PersistencyException 
   * @throws IOException 
   */
  private ReportConfiguration createReportConfiguration(String testReportName, String resultFileName) throws ResourceDataModelException, PersistencyException, IOException
  {
    // copy report from bundle
    File reportBundleFile = EclipsePlatformUtils.extractBundleEntry(ReportingUiTestsPlugin.getDefault().getBundle(), "testResources/" + testReportName);
    File reportFile = new File(fTempReportSourceDirectory, testReportName);
    FileUtil.copy(reportBundleFile, reportFile);
    // copy library from bundle
    File libBundleFile = EclipsePlatformUtils.extractBundleEntry(ReportingUiTestsPlugin.getDefault().getBundle(), "testResources/Xpert.ivy.rptlibrary");
    FileUtil.copy(libBundleFile, new File(fTempReportSourceDirectory, "Xpert.ivy.rptlibrary"));
    
    ReportElementFilter filter = createAndFillFilter();
    ReportConfiguration reportConfiguration = new ReportConfiguration(Arrays.asList("html"), 
            fTempReportGenerationDirectory.getAbsolutePath() + "/",
            resultFileName, reportFile.getAbsolutePath(),
            filter, "Logo", "Title", "Header", "Footer");
    return reportConfiguration;
  }
  
  /**
   * @return a filter full of ivy project data
   * @throws ResourceDataModelException
   * @throws PersistencyException
   */
  private ReportElementFilter createAndFillFilter() throws ResourceDataModelException, PersistencyException
  {
    ReportElementFilter filter = new ReportElementFilter();
    filter.add(DesignerIDEPlugin.getApplicationConfigurationManager().getApplications().get(0));
    IIvyProject project = getProject(TEST_PROJECT_NAME);
    Set<IDataClass> dataClasses = project.getDataClasses(null);
    for(IDataClass dataclass: dataClasses)
    {
      filter.add(dataclass);
    }
    
    Set<IProcess> processes = project.getProcesses(null);
    for (IProcess process: processes)
    {
      filter.add(process);
    }
    
    filter.add(project.getProcessModelVersion());
    
    Set<IRichDialog> richDialogs = project.getProjectRichDialogManager().getRichDialogs(null);
    for (IRichDialog richDialog: richDialogs)
    {
      filter.add(richDialog);
    }
    
    filter.add(project.getProcessModelVersion().getProcessModel());
    
    return filter;
  }
  
}
