package ch.ivyteam.ivy.reporting.internal.dataset.entry;

import ch.ivyteam.ivy.components.ZObject;
import ch.ivyteam.ivy.richdialog.config.IRichDialog;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;

/**
 * The flattened representation of a rich dialog for the report.
 * @author jst
 * @since 19.06.2009
 */
public class RichDialogReportDataEntry
{

  /** The project report data entry. */
  private ProjectReportDataEntry project;

  /** The Section of this RD. */
  private String rdSection;

  /** The Section of this RD namespace. */
  private String rdNamespaceSection;

  /** The Rich Dialog */
  private IRichDialog richDialog;

  /** Rich dialog screenshot */
  private byte[] richDialogImage;
  
  /** The Process */
  private ZObject process;

  /** The process image. 300 dpi */
  private byte[] processImageHighQuality;

  /** The process image. 96 dpi */
  private byte[] processImageLowQuality;

  /** The Process Element */
  private ZObject processElement;

  /** The data class */
  private IDataClass dataClass;
  
  /** the process identifier */
  private int processId;
  
  /** The data class identifier */
  private int dataClassId;


  /**
   * Constructor
   * @param _project
   * @param _rdSection
   * @param _rdNamespaceSection
   * @param _richDialog
   * @param _richDialogImage
   * @param _process
   * @param _processImageHighQuality
   * @param _processImageLowQuality
   * @param _processElement
   * @param _dataClass
   * @param _processId 
   * @param _dataClassId 
   */
  public RichDialogReportDataEntry(ProjectReportDataEntry _project, String _rdSection,
          String _rdNamespaceSection,
          IRichDialog _richDialog, 
          byte[] _richDialogImage,
          ZObject _process, 
          byte[] _processImageHighQuality, 
          byte[] _processImageLowQuality, 
          ZObject _processElement,
          IDataClass _dataClass, int _processId, int _dataClassId)
  {
    project = _project;
    rdSection = _rdSection;
    rdNamespaceSection = _rdNamespaceSection;
    richDialog = _richDialog;
    richDialogImage = _richDialogImage;
    process = _process;
    processImageHighQuality = _processImageHighQuality;
    processImageLowQuality = _processImageLowQuality;
    processElement = _processElement;
    dataClass = _dataClass;
    this.processId = _processId;
    this.dataClassId = _dataClassId;
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
   * Returns the rdSection
   * @return the rdSection
   */
  public String getRdSection()
  {
    return rdSection;
  }

  /**
   * Returns the rdNamespaceSection
   * @return the rdNamespaceSection
   */
  public String getRdNamespaceSection()
  {
    return rdNamespaceSection;
  }

  /**
   * Returns the richDialog
   * @return the richDialog
   */
  public IRichDialog getRichDialog()
  {
    return richDialog;
  }

  /**
   * Returns the richDialogImage
   * @return the richDialogImage
   */
  public byte[] getRichDialogImage()
  {
    return richDialogImage;
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
   * @param highQuality 
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
   * Returns the dataClass
   * @return the dataClass
   */
  public IDataClass getDataClass()
  {
    return dataClass;
  }
  
  /**
   * Returns the dataClassId
   * @return the dataClassId
   */
  public int getDataClassId()
  {
    return dataClassId;
  }
  
  /**
   * Returns the processId
   * @return the processId
   */
  public int getProcessId()
  {
    return processId;
  }
}
