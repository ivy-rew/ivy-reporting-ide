package ch.ivyteam.ivy.reporting.restricted;

import ch.ivyteam.ivy.components.ZObject;
import ch.ivyteam.ivy.project.IIvyProject;

/**
 * The classes implementing this interface may create Images of Processes. As the implementations of this
 * interface may require designer/server specific code, this plugin only provides the interface of image
 * generators.
 * 
 * @author jst
 * @since 07.08.2009
 */
public interface IProcessImageGenerator
{

  /**
   * Fetches the Process image of a process, scales the image down if necessary and returns a byte array
   * representation of this image.
   * @param ivyProject The IIvyProject the process belongs to
   * @param process The process ZObject
   * @param highQuality if true returns a 300 dpi image otherwise a 96 dpi image
   * @return The scaled process image as byte array
   */
  public byte[] getScaledByteArrayProcessImage(IIvyProject ivyProject, ZObject process, boolean highQuality);

}