package ch.ivyteam.ivy.reporting.restricted.config;

import java.util.HashSet;
import java.util.Set;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.application.IProcessModel;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.richdialog.config.IRichDialog;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;

/**
 * Decides which Applications, ProcessModels, ProcessElements etc. are to be reported.
 * 
 * @author jst
 * @since 18.06.2009
 */
// TODO jst: Check that an element can only be added if it's parent is added too.
public class ReportElementFilter
{
  /** The IApplications to report. */
  private Set<IApplication> applications;

  /** The IProcessModels to report. */
  private Set<IProcessModel> processModels;

  /** The IProcessModelVersions to report. */
  private Set<IProcessModelVersion> processModelVersions;

  /** The IProcesses to report. */
  private Set<IProcess> processes;

  /** The IRichDialogs to report. */
  private Set<IRichDialog> richDialogs;

  /** The IDataClasses to report. */
  private Set<IDataClass> dataClasses;

  /** The maximal nesting depth of composites that is allowed. */
  private int maxNestingDepth;

  /**
   * Constructor
   */
  public ReportElementFilter()
  {
    this.applications = new HashSet<IApplication>();
    this.processModels = new HashSet<IProcessModel>();
    this.processModelVersions = new HashSet<IProcessModelVersion>();
    this.processes = new HashSet<IProcess>();
    this.richDialogs = new HashSet<IRichDialog>();
    this.dataClasses = new HashSet<IDataClass>();
  }

  /**
   * Returns the applications
   * @return the applications
   */
  public Set<IApplication> getApplications()
  {
    return applications;
  }

  /**
   * Returns the maxNestingDepth
   * @return the maxNestingDepth
   */
  public int getMaxNestingDepth()
  {
    return maxNestingDepth;
  }

  /**
   * Sets the maxNestingDepth to the given parameter
   * @param _maxNestingDepth the maxNestingDepth to set
   */
  public void setMaxNestingDepth(int _maxNestingDepth)
  {
    this.maxNestingDepth = _maxNestingDepth;
  }

  /**
   * Add an IApplication to report.
   * @param app
   */
  public void add(IApplication app)
  {
    this.applications.add(app);
  }

  /**
   * Add an IProcessModel to report.
   * @param pm
   */
  public void add(IProcessModel pm)
  {
    this.processModels.add(pm);
  }

  /**
   * Add an IProcessModelVersion to report.
   * @param pmv
   */
  public void add(IProcessModelVersion pmv)
  {
    this.processModelVersions.add(pmv);
  }

  /**
   * Add an IProcess to report.
   * @param proc
   */
  public void add(IProcess proc)
  {
    this.processes.add(proc);
  }

  /**
   * Add an IRichDialog to report.
   * @param rd
   */
  public void add(IRichDialog rd)
  {
    this.richDialogs.add(rd);
  }

  /**
   * Add an IDataClass to report.
   * @param dataClass
   */
  public void add(IDataClass dataClass)
  {
    this.dataClasses.add(dataClass);
  }

  /**
   * Decide whether an IApplication should be reported.
   * @param app The Application.
   * @return Should we report this Application.
   */
  public boolean accept(IApplication app)
  {
    return applications.contains(app);
  }

  /**
   * Decide whether an IProcessModel should be reported.
   * @param pm The ProcessModel.
   * @return Should we report this ProcessModel.
   */
  public boolean accept(IProcessModel pm)
  {
    return processModels.contains(pm);
  }

  /**
   * Decide whether an IProcessModelVersion should be reported.
   * @param pmv The ProcessModelVersion.
   * @return Should we report this ProcessModelVersion.
   */
  public boolean accept(IProcessModelVersion pmv)
  {
    return processModelVersions.contains(pmv);
  }

  /**
   * Decide whether an IProcess should be reported.
   * @param proc The Process.
   * @return Should we report this Process.
   */
  public boolean accept(IProcess proc)
  {
    return processes.contains(proc);
  }

  /**
   * Decide whether an IRichDialog should be reported.
   * @param rd The RichDialog.
   * @return Should we report this RichDialog.
   */
  public boolean accept(IRichDialog rd)
  {
    return richDialogs.contains(rd);
  }

  /**
   * Decide whether an IDataClass should be reported.
   * @param dataClass The DataClass.
   * @return Should we report this DataClass.
   */
  public boolean accept(IDataClass dataClass)
  {
    return dataClasses.contains(dataClass);
  }
}
