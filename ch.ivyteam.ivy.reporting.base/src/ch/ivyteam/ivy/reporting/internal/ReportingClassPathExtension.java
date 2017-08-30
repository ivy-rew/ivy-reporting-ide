package ch.ivyteam.ivy.reporting.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.JavaCore;

import ch.ivyteam.ivy.java.IIvyProjectClassPathExtension;
import ch.ivyteam.ivy.reporting.extension.BirtReportGeneratorPI;

public class ReportingClassPathExtension implements IIvyProjectClassPathExtension
{
  public static final String BUNDLE_ID = "ch.ivyteam.ivy.reporting.base";
  
  @Override
  public List<String> getCompileClassPathContributingBundles()
  {
    return Arrays.asList(BUNDLE_ID);
  }

  @Override
  public List<IAccessRule> getCompileClassPathAccessRules(String bundleIdentifier)
  {
    if (!BUNDLE_ID.equals(bundleIdentifier))
    {
      return Collections.emptyList();
    }
    
    List<IAccessRule> accessRules = new ArrayList<>();
    String accessRulesString = BirtReportGeneratorPI.class.getName();
    List<String> accessiblePaths = Arrays.asList(accessRulesString.replace('.', '/').split(","));
    Iterator<String> it = accessiblePaths.iterator();
    while (it.hasNext())
    {
      String path = it.next();
      accessRules.add(JavaCore.newAccessRule(new Path(path), 0));
    }
    accessRules.add(EXCLUDE_ALL_OTHER_RULE);
    return accessRules;
  }

  @Override
  public List<String> getCompileClassPath(String bundleIdentifier)
  {
    return Collections.emptyList();
  }

  @Override
  public List<String> getClassLoaderContributingBundles()
  {
    return Arrays.asList(BUNDLE_ID);
  }

}
