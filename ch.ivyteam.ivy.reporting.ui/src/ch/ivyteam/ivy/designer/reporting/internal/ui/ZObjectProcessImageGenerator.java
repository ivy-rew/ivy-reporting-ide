package ch.ivyteam.ivy.designer.reporting.internal.ui;

import ch.ivyteam.ivy.components.ZObject;
import ch.ivyteam.ivy.designer.process.image.ProcessImageGenerator;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.reporting.restricted.IProcessImageGenerator;

@SuppressWarnings("restriction")
public class ZObjectProcessImageGenerator extends ProcessImageGenerator implements IProcessImageGenerator
{
  @Override
  public byte[] getScaledByteArrayProcessImage(IIvyProject ivyProject, ZObject process, boolean highQuality)
  {
    return super.getScaledByteArrayProcessImage(ivyProject, process, highQuality);
  }
}
