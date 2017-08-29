package ch.ivyteam.ivy.reporting.internal.dataset.entry;

import ch.ivyteam.ivy.components.ZObject;

/**
 * The flattened representation of a process for the report.
 * @author jst
 * @since 19.06.2009
 */
public class ProcessReportDataEntry
{

  /** The project report data entry. */
  private ProjectReportDataEntry project;

  /** The Process Section. */
  private String processSection;

  /** The Process Group Section. */
  private String processGroupSection;

  /** The process. */
  private ZObject process;

  /** The process image with 300 dpi. */
  private byte[] processImageHighQuality;

  /** The process image with 96 dpi. */
  private byte[] processImageLowQuality;

  /** The process element. */
  private ZObject processElement;
  
  /** the identifier */
  private int id;

  /**
   * Constructor
   * @param _project
   * @param _processSection
   * @param _processGroupSection
   * @param _process
   * @param _processImageHighQuality
   * @param _processImageLowQuality 
   * @param _processElement
   * @param _id 
   */
  public ProcessReportDataEntry(ProjectReportDataEntry _project, String _processSection,
          String _processGroupSection, ZObject _process, byte[] _processImageHighQuality,
          byte[] _processImageLowQuality, ZObject _processElement, int _id)
  {
    this.project = _project;
    this.processSection = _processSection;
    this.processGroupSection = _processGroupSection;
    this.process = _process;
    this.processImageHighQuality = _processImageHighQuality;
    this.processImageLowQuality = _processImageLowQuality;
    this.processElement = _processElement;
    this.id = _id;
  }

  /**
   * Returns the project
   * @return the project
   */
  public ProjectReportDataEntry getProject()
  {
    return project;
  }

  /**
   * Returns the processSection
   * @return the processSection
   */
  public String getProcessSection()
  {
    return processSection;
  }

  /**
   * Returns the processGroupSection
   * @return the processGroupSection
   */
  public String getProcessGroupSection()
  {
    return processGroupSection;
  }

  /**
   * Returns the process
   * @return the process
   */
  public ZObject getProcess()
  {
    return process;
  }

  /**
   * Returns the processImage
   * @param highQuality if true a high quality 300 dpi process image is returned otherwise a 96 dpi process image is returned
   * @return the processImage
   */
  public byte[] getProcessImage(boolean highQuality)
  {
    return highQuality?processImageHighQuality:processImageLowQuality;
  }

  /**
   * Returns the processElement
   * @return the processElement
   */
  public ZObject getProcessElement()
  {
    return processElement;
  }
  
  /** 
   * @return identifier
   */
  public int getId()
  {
    return id;
  }

}
