package ch.ivyteam.ivy.reporting.restricted;

/**
 * Allows Process Image Generators to be registered for the Reporting plugin. In order for birt reports to
 * contain process images, an IProcessImageGenerator has to be registered. This should be done by the Server
 * or the Designer.
 * 
 * @author jst
 * @since 07.08.2009
 */
public class ImageGeneratorRegistry
{
  /** The IProcessImageGenerator. */
  private static IProcessImageGenerator processImageGenerator;

  /**
   * Get the registered IProcessImageGenerator.
   * @return A IProcessImageGenerator.
   */
  public static IProcessImageGenerator getProcessImageGenerator()
  {
    return processImageGenerator;
  }

  /**
   * Registers a IProcessImageGenerator.
   * @param _processImageGenerator
   */
  public static void setProcessImageGenerator(IProcessImageGenerator _processImageGenerator)
  {
    processImageGenerator = _processImageGenerator;
  }

}
