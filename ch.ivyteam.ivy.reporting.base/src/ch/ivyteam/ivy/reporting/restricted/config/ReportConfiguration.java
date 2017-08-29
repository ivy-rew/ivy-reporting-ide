package ch.ivyteam.ivy.reporting.restricted.config;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Configuration for a Birt Report.
 * 
 * @author jst
 * @since 18.06.2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reportConfig")
@XmlRootElement
public class ReportConfiguration implements Serializable
{
  /**
   * The Serialisation Id.
   */
  private static final long serialVersionUID = 1L;

  /** The output Format of the Report. E.g. html, pdf, doc, etc... */
  @XmlElementWrapper(name = "reportFormats")
  @XmlElement(name = "format")
  private List<String> reportFormats;

  /** The Place where Reports are to be stored. */
  private String fileSystemRoot;

  /** The name of the output file. */
  private String reportFileName;

  /** The report Design file. */
  private String reportDesign;

  /** The Filters the Elements to be reported. */
  transient private ReportElementFilter filter;

  /** The Corporate Identity Logo. */
  private String logo;

  /** The Corporate Identity Title. */
  private String title;

  /** The Corporate Identity Header. */
  private String header;

  /** The Corporate Identity Footer. */
  private String footer;

  /** Default No-args constructor for Jaxb. */
  public ReportConfiguration()
  {
  }

  /**
   * Constructor
   * @param _reportFormats The output formats of the report (pdf, doc, html,...)
   * @param _fileSystemRoot The destination folder
   * @param _reportFileName The filename (without extension)
   * @param _reportDesign The *.rptdesign file path
   * @param _filter The report element filter
   * @param _logo The Corporate Identity Logo
   * @param _title The Corporate Identity Title
   * @param _header The Corporate Identity Header
   * @param _footer The Corporate Identity Footer
   */
  public ReportConfiguration(List<String> _reportFormats, String _fileSystemRoot, String _reportFileName,
          String _reportDesign, ReportElementFilter _filter,
          String _logo, String _title, String _header, String _footer)
  {
    this.reportFormats = _reportFormats;
    this.fileSystemRoot = _fileSystemRoot;
    this.reportFileName = _reportFileName;
    this.reportDesign = _reportDesign;
    this.filter = _filter;
    this.logo = _logo;
    this.title = _title;
    this.header = _header;
    this.footer = _footer;
  }

  /**
   * @return Absolute filename including path, filename and dot '.' but without file type.
   */
  public String getAbsoluteReportFileName()
  {
    return getFileSystemRoot() + getReportFileName() + ".";
  }

  /**
   * Returns the reportFormats
   * @return the reportFormats
   */
  public List<String> getReportFormats()
  {
    return reportFormats;
  }

  /**
   * Returns the fileSystemRoot
   * @return the fileSystemRoot
   */
  public String getFileSystemRoot()
  {
    return fileSystemRoot;
  }

  /**
   * Returns the reportFileName
   * @return the reportFileName
   */
  public String getReportFileName()
  {
    return reportFileName;
  }

  /**
   * Returns the reportDesign
   * @return the reportDesign
   */
  public String getReportDesign()
  {
    return reportDesign;
  }

  /**
   * Returns the filter
   * @return the filter
   */
  public ReportElementFilter getFilter()
  {
    return filter;
  }

  /**
   * Returns the reportLogo
   * @return the reportLogo
   */
  public String getLogo()
  {
    return logo;
  }

  /**
   * Returns the title
   * @return the title
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * Returns the header
   * @return the header
   */
  public String getHeader()
  {
    return header;
  }

  /**
   * Returns the footer
   * @return the footer
   */
  public String getFooter()
  {
    return footer;
  }

  /**
   * Returns the withDetails
   * @return the withDetails
   */
  public boolean isWithDetails()
  {
    return false;
  }

  /**
   * Sets the reportFormats to the given parameter
   * @param _reportFormats the reportFormats to set
   */
  public void setReportFormats(List<String> _reportFormats)
  {
    this.reportFormats = _reportFormats;
  }

  /**
   * Sets the fileSystemRoot to the given parameter
   * @param _fileSystemRoot the fileSystemRoot to set
   */
  public void setFileSystemRoot(String _fileSystemRoot)
  {
    this.fileSystemRoot = _fileSystemRoot;
  }

  /**
   * Sets the reportFileName to the given parameter
   * @param _reportFileName the reportFileName to set
   */
  public void setReportFileName(String _reportFileName)
  {
    this.reportFileName = _reportFileName;
  }

  /**
   * Sets the reportDesign to the given parameter
   * @param _reportDesign the reportDesign to set
   */
  public void setReportDesign(String _reportDesign)
  {
    this.reportDesign = _reportDesign;
  }

  /**
   * Sets the filter to the given parameter
   * @param _filter the filter to set
   */
  public void setFilter(ReportElementFilter _filter)
  {
    this.filter = _filter;
  }

  /**
   * Sets the reportLogo to the given parameter
   * @param _logo the reportLogo to set
   */
  public void setLogo(String _logo)
  {
    this.logo = _logo;
  }

  /**
   * Sets the title to the given parameter
   * @param _title the title to set
   */
  public void setTitle(String _title)
  {
    this.title = _title;
  }

  /**
   * Sets the header to the given parameter
   * @param _header the header to set
   */
  public void setHeader(String _header)
  {
    this.header = _header;
  }

  /**
   * Sets the footer to the given parameter
   * @param _footer the footer to set
   */
  public void setFooter(String _footer)
  {
    this.footer = _footer;
  }

}
