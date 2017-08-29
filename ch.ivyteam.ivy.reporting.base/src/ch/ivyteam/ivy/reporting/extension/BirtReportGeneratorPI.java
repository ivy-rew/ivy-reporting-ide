package ch.ivyteam.ivy.reporting.extension;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.ivyteam.di.restricted.DiCore;
import ch.ivyteam.ivy.process.engine.IRequestId;
import ch.ivyteam.ivy.process.extension.IIvyScriptEditor;
import ch.ivyteam.ivy.process.extension.IProcessExtensionConfigurationEditorEnvironment;
import ch.ivyteam.ivy.process.extension.IProcessExtensionConfigurationEditorEx;
import ch.ivyteam.ivy.process.extension.impl.AbstractUserProcessExtension;
import ch.ivyteam.ivy.reporting.restricted.IBirtRuntimeManager;
import ch.ivyteam.ivy.scripting.language.IIvyScriptContext;
import ch.ivyteam.ivy.scripting.objects.CompositeObject;
import ch.ivyteam.log.Logger;

/**
 * This class is an implementation of the Ivy PI-Element. It allows to create Reports using the already
 * started birt report engine.
 * @author jst
 * @since 10.08.2009
 */
// TODO: needs test
// TODO: provide - as a service - the filling of a "in" property, e.g. "generatedReports" of type List<String> or List<File> that will be set when present
public class BirtReportGeneratorPI extends AbstractUserProcessExtension
{
  private static final Logger LOGGER = Logger.getClassLogger(BirtReportGeneratorPI.class);

  /**
   * @see ch.ivyteam.ivy.process.extension.IUserProcessExtension#perform(ch.ivyteam.ivy.process.engine.IRequestId,
   *      ch.ivyteam.ivy.scripting.objects.CompositeObject,
   *      ch.ivyteam.ivy.scripting.language.IIvyScriptContext)
   */
  @SuppressWarnings("unchecked")
  @Override
  public CompositeObject perform(IRequestId requestId, CompositeObject in, IIvyScriptContext context)
  throws Exception
  {
    String reportName = "";
    String reportPath = "";
    String designPath = "";
    List<String> formats = Arrays.asList("pdf", "html", "doc"); // Default values
    Map<String, Object> reportParameters = new HashMap<String, Object>();

    // FIXME: This '|' is a pretty stupid separator for scripting parts, as it is a script operator itself! 
    // Use something else, e.g. an unlikely multichar-separator such as "@@@"
    
    StringTokenizer st = new StringTokenizer(getConfiguration(), "|"); 
    if (st.hasMoreElements())
    {
      reportName = (String) executeIvyScript(context, st.nextToken());
    }
    if (st.hasMoreElements())
    {
      reportPath = (String) executeIvyScript(context, st.nextToken());
    }
    if (st.hasMoreElements())
    {
      designPath = (String) executeIvyScript(context, st.nextToken());
      
    }
    try
    {
      if (st.hasMoreElements())
      {
        formats = (List<String>) executeIvyScript(context, st.nextToken());
      }
    }
    catch (Exception ex)
    {
      LOGGER.warn("Could not get report format list: "+getConfiguration());
    }
    try
    {
      if (st.hasMoreElements())
      {
        reportParameters = (Map<String, Object>) executeIvyScript(context, st.nextToken());
      }
    }
    catch (Exception ex)
    {
      LOGGER.warn("Could not get Parameter Map: "+getConfiguration(), ex);
    }

    File outputDir = new File(reportPath);
    LOGGER.debug("Report Destination: " + outputDir + ", Report Name: " + reportName);

    DiCore.getGlobalInjector().getInstance(IBirtRuntimeManager.class).createSimpleReport(designPath, formats, outputDir, reportName, reportParameters);
    return in;
  }

  /**
   * An editor that is called from the PI-inscription mask used to set configuration parameters for the
   * PI-Bean. Provides the configuration as string
   * 
   *@author jst
   *@created 10.08.2009
   */
  public static class Editor extends JPanel implements IProcessExtensionConfigurationEditorEx
  {
    /**  */
    private IProcessExtensionConfigurationEditorEnvironment env;

    /** IvyScriptEditor for the report name */
    private IIvyScriptEditor reportNameEditor;

    /** IvyScriptEditor for the report destination */
    private IIvyScriptEditor reportOutputPathEditor;

    /** IvyScriptEditor for the report design file */
    private IIvyScriptEditor reportDesignEditor;

    /** IvyScriptEditor for the report output formats */
    private IIvyScriptEditor reportFormatEditor;

    /** IvyScriptEditor for the report parameters */
    private IIvyScriptEditor reportParameterEditor;

    /**
     * Constructor for the Editor object
     */
    public Editor()
    {
      super(new GridLayout(5, 2));
    }

    /**
     * Gets the configuration
     * 
     *@return The configuration as an String
     * @deprecated
     */
    @Override
    @Deprecated
    public String getConfiguration()
    {
      StringBuffer configuration = new StringBuffer();
      configuration.append(reportNameEditor.getText() + "|");
      configuration.append(reportOutputPathEditor.getText() + "|");
      configuration.append(reportDesignEditor.getText() + "|");
      configuration.append(reportFormatEditor.getText() + "|");
      configuration.append(reportParameterEditor.getText() + "|");
      return configuration.toString();
    }

    /**
     * Sets the configuration
     * 
     *@param config the configuration as an String
     * @deprecated
     */
    @Override
    @Deprecated
    public void setConfiguration(String config)
    {
      StringTokenizer st = new StringTokenizer(config, "|");
      if (st.hasMoreElements())
        reportNameEditor.setText(st.nextElement().toString());
      if (st.hasMoreElements())
        reportOutputPathEditor.setText(st.nextElement().toString());
      if (st.hasMoreElements())
        reportDesignEditor.setText(st.nextElement().toString());
      if (st.hasMoreElements())
        reportFormatEditor.setText(st.nextElement().toString());
      if (st.hasMoreElements())
        reportParameterEditor.setText(st.nextElement().toString());

    }

    /**
     * Gets the component attribute of the Editor object
     * 
     *@return this
     * @deprecated
     */
    @Override
    @Deprecated
    public Component getComponent()
    {
      return this;
    }

    /**
     *@return boolean
     * @deprecated
     */
    @Override
    @Deprecated
    public boolean acceptInput()
    {
      return true;
    }

    /**
     * @see ch.ivyteam.ivy.process.extension.IProcessExtensionConfigurationEditorEx#setEnvironment(ch.ivyteam.ivy.process.extension.IProcessExtensionConfigurationEditorEnvironment)
     */
    @Override
    public void setEnvironment(IProcessExtensionConfigurationEditorEnvironment environment)
    {
      this.env = environment;
      reportNameEditor = env.createIvyScriptEditor(null, null, "String");
      reportOutputPathEditor = env.createIvyScriptEditor(null, null, "String");
      reportDesignEditor = env.createIvyScriptEditor(null, null, "String");
      reportFormatEditor = env.createIvyScriptEditor(null, null, "List<String>");
      reportParameterEditor = env.createIvyScriptEditor(null, null, "Map<String,Object>");

      add(new JLabel("Report Name"));
      add(reportNameEditor.getComponent());
      add(new JLabel("Report Output Path"));
      add(reportOutputPathEditor.getComponent());
      add(new JLabel("Report Design (*.rptdesign)"));
      add(reportDesignEditor.getComponent());
      add(new JLabel("Report Formats (pdf, html, doc, ppt or xls)"));
      add(reportFormatEditor.getComponent());
      add(new JLabel("Parameters"));
      add(reportParameterEditor.getComponent());
    }
  }
}
