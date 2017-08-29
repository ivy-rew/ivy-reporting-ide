package ch.ivyteam.ivy.reporting.internal.dataset;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.reporting.internal.ReportDataCollector;

/**
 * This is the abstract class that is to be used by all DataSetEventAdapters. It makes sure that the
 * subclasses can access the report data through the reportDataCollector property.
 * 
 * @author jst
 * @since 25.08.2009
 */
public abstract class ReportingDataSetEventAdapter extends ScriptedDataSetEventAdapter
{

  /** The data for the report. */
  protected ReportDataCollector reportDataCollector;
  
  /** A high quality report */
  protected boolean highQuality;

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.DataSetEventAdapter#beforeOpen(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IReportContext)
   */
  @Override
  public void beforeOpen(IDataSetInstance dataSet,
          IReportContext reportContext) throws ScriptException
  {
    reportDataCollector = (ReportDataCollector) reportContext.getAppContext().get("reportDataCollector");
    Boolean hq = (Boolean)reportContext.getAppContext().get("highQuality");
    highQuality = hq==null?false:hq;
  }
}
