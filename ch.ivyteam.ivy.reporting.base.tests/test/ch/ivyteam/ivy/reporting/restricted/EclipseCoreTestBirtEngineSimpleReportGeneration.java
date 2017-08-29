package ch.ivyteam.ivy.reporting.restricted;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.junit.Test;
import org.osgi.framework.Bundle;

import ch.ivyteam.eclipse.util.EclipsePlatformUtils;

/**
 * Tests running of a simple report using the BIRT installation that is
 * installed with ivy. In order for this to work this must run as a
 * Plugin-Test (OSGI must be started).
 * @author kvg
 * @since 28.04.2010
 */
public class EclipseCoreTestBirtEngineSimpleReportGeneration
{
  /** The "reporting base" plugin id */
  private final static String REPORTING_BASE_TESTS_BUNDLE = "ch.ivyteam.ivy.reporting.base.tests";
  
  /**
   * Extract a file from this bundle.
   * @param bundleEntryPath 
   * @return extracted report file path
   * @throws IOException
   */
  public static String extractFromBundle(String bundleEntryPath) throws IOException
  {
    Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(REPORTING_BASE_TESTS_BUNDLE);
    return EclipsePlatformUtils.extractBundleEntry(bundle, bundleEntryPath).getAbsolutePath();
  }

  /**
   * Creates a test report from an *.rptdesign bundle entry.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCreateReportFromBundle() throws Exception
  {
    String rptDesignFilePath = extractFromBundle("/testResources/testReport.rptdesign");
    
    EngineConfig config = new EngineConfig();
    config.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader());

    // Create the report engine
    IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
    IReportEngine engine = factory.createReportEngine(config);
    IReportRunnable design = null;

    // create report task
    design = engine.openReportDesign(rptDesignFilePath);
    IRunAndRenderTask task = engine.createRunAndRenderTask(design);

    // set render options
    HTMLRenderOption options = new HTMLRenderOption();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    options.setOutputStream(bos);
    options.setOutputFormat("html");
    task.setRenderOption(options);
    
    // run task
    task.run();
    task.close();

    // output and test
    String generated = bos.toString();
    assertThat(generated).contains("src=\"http://zugntsrv1/snipsnap/space/SnipSnap/config/ivyteam.jpg\"");
    assertThat(generated).contains("Hello World!");
    assertThat(generated).contains("Hello Birt!");
    assertThat(generated).contains("2010");
    
    // clean up
    engine.destroy();
  }
}
