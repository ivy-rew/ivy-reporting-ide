package ch.ivyteam.ivy.reporting.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.ivyteam.eclipse.util.EclipseUtil;
import ch.ivyteam.ivy.manager.restricted.AbstractManager;
import ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager;
import ch.ivyteam.ivy.reporting.restricted.IReportingManager;
import ch.ivyteam.ivy.reporting.restricted.ReportingException;
import ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration;
import ch.ivyteam.ivy.server.IServerExtension;
import ch.ivyteam.log.Logger;

/**
 * The Reporting Manager is responsible for creating Birt reports. It is also responsible for reading and
 * storing report configurations.
 * 
 * @author jst
 */
@Singleton
public class ReportingManager extends AbstractManager implements IReportingManager, IServerExtension
{
  private static final Logger LOGGER = Logger.getPackageLogger(ReportingManager.class);

  /** The birt runtime manager */
  private IBirtRuntimeManager birtRuntimeManager;

  /** The birt report engine */
  private IReportEngine reportEngine;

  /** The birt design engine */
  private IDesignEngine designEngine;

  /** The JAXB Context. */
  private JAXBContext jaxbContext;
  
  /**
   * Constructor
   * @param _birtRuntimeManager
   */
  @Inject
  public ReportingManager(IBirtRuntimeManager _birtRuntimeManager)
  {
    assert _birtRuntimeManager != null : "BirtRuntimeManager must not be null!";
    birtRuntimeManager = _birtRuntimeManager;
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#getName()
   */
  @Override
  public String getName()
  {
    return "Reporting Manager"+( (designEngine == null||reportEngine == null) ? " [Disabled]" : "");
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#start(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void doStart(SubMonitor monitor) throws ReportingException
  {
    initializeJaxb();
    reportEngine = birtRuntimeManager.getBirtEngine(IReportEngine.class, this);
    designEngine = birtRuntimeManager.getBirtEngine(IDesignEngine.class, this);
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#stop(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void doStop(SubMonitor monitor)
  {
    reportEngine = null;
    designEngine = null;
    jaxbContext = null;
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#isReady()
   */
  @Override
  public boolean isReady()
  {
    if (birtRuntimeManager != null &&
        birtRuntimeManager.isStarted() &&
        jaxbContext != null)
    {
      return true;
    }
    return false;
  }

  /**
   * Initializes the JAXB Context and the (Un-)Marshallers
   * @throws ReportingException
   */
  private void initializeJaxb() throws ReportingException
  {
    LOGGER.debug("Initializing Jaxb...");
    // Set the ContextClassLoader of the current thread, so that the JAXBContext
    // can load the required classes
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      jaxbContext = JAXBContext.newInstance(ReportConfiguration.class);
    }
    catch (JAXBException ex)
    {
      throw new ReportingException("JAXB could not be initialized.", ex);
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
    LOGGER.debug("done!");
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#persistReportConfiguration(ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration,
   *      java.lang.String)
   */
  @Override
  public void persistReportConfiguration(ReportConfiguration reportConfig, String configPath)
          throws ReportingException
  {
    FileWriter writer = null;
    try
    {
      writer = new FileWriter(configPath);
      Marshaller m = jaxbContext.createMarshaller();
      m.marshal(reportConfig, writer);
    }
    catch (JAXBException ex)
    {
      throw new ReportingException("XML creation failed!", ex);
    }
    catch (IOException ex)
    {
      throw new ReportingException("Error writing report configuration to disk!", ex);
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
        }
        catch (IOException ex)
        {
          throw new ReportingException("Error closing file writer.", ex);
        }
      }
    }
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#loadReportConfiguration(java.lang.String)
   */
  @Override
  public ReportConfiguration loadReportConfiguration(String configPath) throws ReportingException
  {
    ReportConfiguration reportConfig = null;
    FileReader reader = null;
    try
    {
      reader = new FileReader(configPath);
      Unmarshaller um = jaxbContext.createUnmarshaller();
      reportConfig = (ReportConfiguration) um.unmarshal(reader);
    }
    catch (JAXBException ex)
    {
      throw new ReportingException("XML reading failed!", ex);
    }
    catch (IOException ex)
    {
      throw new ReportingException("Error reading report configuration to disk!", ex);
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException ex)
        {
          throw new ReportingException("Error closing file reader.", ex);
        }
      }
    }
    return reportConfig;
  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#runAndRenderReport(ch.ivyteam.ivy.reporting.restricted.config.ReportConfiguration,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void runAndRenderReport(ReportConfiguration reportConfig, IProgressMonitor monitor)
          throws ReportingException
  {
    boolean needHighQuality = false;
    boolean needLowQuality = false;
    monitor = EclipseUtil.ensureProgressMonitor(monitor);
    
    needHighQuality = reportConfig.getReportFormats().contains("pdf");
    needLowQuality = reportConfig.getReportFormats().contains("html")||reportConfig.getReportFormats().contains("doc");

    int steps = reportConfig.getReportFormats().size() + 11 + (needHighQuality?1:0) + (needLowQuality?1:0);
    monitor.beginTask("Generating Reports", steps);

    // Collect report data
    LOGGER.debug("Fetching report data");
    ReportDataCollector reportDataCollector = new ReportDataCollector();
    reportDataCollector.fetchReportData(reportConfig, needHighQuality, needLowQuality, new SubProgressMonitor(monitor, 10, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

    // Path of the temporary filledReportDocument
    String reportDocumentPathHighQuality = null;
    String reportDocumentPathLowQuality = null;

    IReportDocument reportDocumentHighQuality = null;
    IReportDocument reportDocumentLowQuality = null;
    try
    {
      // Open the report design and set the theme
      LOGGER.debug("Opening report design...");
      IReportRunnable runnableReportDesign = openAndThemeReportDesign(reportConfig, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

      // Fill the report with data
      LOGGER.debug("Filling report with data...");

      if (needHighQuality)
      {
        reportDocumentPathHighQuality = new Path(reportConfig.getAbsoluteReportFileName() + "hq_rptdocument").toOSString();
        reportDocumentHighQuality = fillReportDocument(reportConfig, reportDocumentPathHighQuality, runnableReportDesign, reportDataCollector, true, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
      }
      
      if (needLowQuality)
      {
        reportDocumentPathLowQuality = new Path(reportConfig.getAbsoluteReportFileName() + "lq_rptdocument").toOSString();
        reportDocumentLowQuality = fillReportDocument(reportConfig, reportDocumentPathLowQuality, runnableReportDesign, reportDataCollector, false, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
      }

      // Render the report for the multiple output formats
      for (String format : reportConfig.getReportFormats())
      {
        LOGGER.debug("Rendering " + format + " report...");
        renderReportFormat(reportConfig, reportDocumentHighQuality, reportDocumentLowQuality, format, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
      }
      LOGGER.debug("All rendering done!");
    }
    finally
    {
      // Close and delete the filled report document
      if (reportDocumentHighQuality != null)
      {
        reportDocumentHighQuality.close();
        File reportDocumentFile = new File(reportDocumentPathHighQuality);
        if (reportDocumentFile.exists() && reportDocumentFile.canWrite())
        {
          if (!reportDocumentFile.delete())
          {
            LOGGER.debug("Could not delete report document file: " + reportDocumentPathHighQuality);
          }
        }
      }
      if (reportDocumentLowQuality!=null)
      {
        reportDocumentLowQuality.close();
        File reportDocumentFile = new File(reportDocumentPathLowQuality);
        if (reportDocumentFile.exists() && reportDocumentFile.canWrite())
        {
          if (!reportDocumentFile.delete())
          {
            LOGGER.debug("Could not delete report document file: " + reportDocumentPathLowQuality);
          }
        }
      }
      monitor.done();
    }
  }

  /**
   * Open the report design and set the theme.
   * 
   * @param reportConfig
   * @param progressMonitor 
   * @return A runnable report design
   * @throws ReportingException
   */
  private IReportRunnable openAndThemeReportDesign(ReportConfiguration reportConfig, IProgressMonitor progressMonitor)
          throws ReportingException
  {
    IReportRunnable runnableReportDesign = null;
    progressMonitor.beginTask("Opening report design ...", 4);
    progressMonitor.subTask("");
    try
    {
      SessionHandle session = designEngine.newSessionHandle(null);
      progressMonitor.worked(1);
      ReportDesignHandle designHandle = session.openDesign(reportConfig.getReportDesign());
      progressMonitor.worked(1);
      runnableReportDesign = reportEngine.openReportDesign(designHandle);
      progressMonitor.worked(1);
      designHandle.close();
      progressMonitor.worked(1);
    }
    catch (DesignFileException ex)
    {
      LOGGER.error("Could not open design file " + reportConfig.getReportDesign(), ex);
      throw new ReportingException("Could not open design file " + reportConfig.getReportDesign(), ex);
    }
    catch (EngineException ex)
    {
      LOGGER.error("Could not open report design file " + reportConfig.getReportDesign(), ex);
      throw new ReportingException("Could not open report design file " + reportConfig.getReportDesign(), ex);
    }
    finally
    {
      progressMonitor.done();
    }
    return runnableReportDesign;
  }

  /**
   * Fill the report design with data and write the document to reportDocumentPath.
   * 
   * @param reportConfig
   * @param reportDocumentPath
   * @param runnableReportDesign
   * @param dataCollector 
   * @param highQuality 
   * @param progressMonitor 
   * @return The ReportDocument
   * @throws ReportingException
   */
  @SuppressWarnings("unchecked")
  private IReportDocument fillReportDocument(
          ReportConfiguration reportConfig,
          String reportDocumentPath, 
          IReportRunnable runnableReportDesign,
          ReportDataCollector dataCollector,
          boolean highQuality, 
          IProgressMonitor progressMonitor)
  throws ReportingException
  {
    progressMonitor.beginTask("Fill report document "+reportDocumentPath, 2);
    progressMonitor.subTask("");
    try
    {
      IRunTask runTask = reportEngine.createRunTask(runnableReportDesign);
      setReportParameters(reportConfig, runTask);
      runTask.getAppContext().put("reportDataCollector", dataCollector);
      runTask.getAppContext().put("highQuality", highQuality);
      runTask.run(reportDocumentPath);
      progressMonitor.worked(1);
      runTask.close();
      return reportEngine.openReportDocument(reportDocumentPath);      
    }
    catch (EngineException ex)
    {
      LOGGER.error("Filling the report failed!", ex);
      throw new ReportingException("Filling the report failed!", ex);
    }
    finally
    {
      progressMonitor.done();
    }
  }

  /**
   * Sets the report parameters.
   * 
   * @param reportConfig
   * @param runTask
   */
  private void setReportParameters(ReportConfiguration reportConfig, IRunTask runTask)
  {
    // Set whether technical details should be show as Parameter for the Report. The individual Report
    // Elements should be hidden if this parameter is false.
    runTask.setParameterValue("Technical Details", reportConfig.isWithDetails());

    // The Corporate Identity Strings
    runTask.setParameterValue("Title", reportConfig.getTitle());
    runTask.setParameterValue("Header", reportConfig.getHeader());
    runTask.setParameterValue("Footer", reportConfig.getFooter());

  }

  /**
   * Render the given filled report document for a given output format.
   * 
   * @param reportConfig
   * @param reportDocumentHighQuality
   * @param reportDocumentLowQuality 
   * @param format
   * @param progressMonitor 
   * @throws ReportingException
   */
  private void renderReportFormat(ReportConfiguration reportConfig,
          IReportDocument reportDocumentHighQuality, IReportDocument reportDocumentLowQuality, String format, IProgressMonitor progressMonitor) throws ReportingException
  {
    IReportDocument reportDocument = reportDocumentLowQuality;
    
    if (format.equalsIgnoreCase("pdf"))
    {
      reportDocument = reportDocumentHighQuality;
    }

    IRenderTask renderTask = reportEngine.createRenderTask(reportDocument);

    RenderOption options = new RenderOption();
    options.setOutputFileName(reportConfig.getAbsoluteReportFileName() + format);
    options.setOutputFormat(format);
    // Format Specific Options
    if (format.equalsIgnoreCase("html"))
    {
      HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
      String imageDir = new Path("./" + reportConfig.getReportFileName() + "_images").toOSString();
      htmlOptions.setImageDirectory(imageDir);
      htmlOptions.setUrlEncoding("UTF-8");
    }
    else if (format.equalsIgnoreCase("pdf"))
    {
      PDFRenderOption pdfOptions = new PDFRenderOption(options);
      pdfOptions.setOption(IPDFRenderOption.DPI, 300);
      pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES);
    }
       
    renderTask.setRenderOption(options);

    // render report
    progressMonitor.beginTask("Rendering "+format+" report ...", 1);
    progressMonitor.subTask("");
    try
    {
      renderTask.render();
    }
    catch (EngineException ex)
    {
      LOGGER.error("Rendering of " + format + " report failed!", ex);
      throw new ReportingException("Rendering of " + format + " report failed!", ex);
    }
    finally
    {
      progressMonitor.done();
      renderTask.close();
    }

  }

  /**
   * @see ch.ivyteam.ivy.reporting.restricted.IReportingManager#getAvailableThemes(java.lang.String)
   */
  @Override
  public List<String> getAvailableThemes(String designPath) throws ReportingException
  {
    return birtRuntimeManager.getAvailableThemes(designPath);
  }

  /**
   * Returns the logger
   * @return the logger
   */
  public static Logger getLogger()
  {
    return LOGGER;
  }

  @Override
  public String getIdentifier()
  {
    return ReportingManager.class.getName();
  }
}