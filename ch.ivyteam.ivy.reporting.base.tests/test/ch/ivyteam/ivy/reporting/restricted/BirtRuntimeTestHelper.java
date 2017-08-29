package ch.ivyteam.ivy.reporting.restricted;

import javax.inject.Singleton;

import org.eclipse.core.runtime.NullProgressMonitor;

import ch.ivyteam.test.AbstractRuleTestHelper;

import javax.inject.Inject;

/**
 * Helper for BirtRuntimeTests.
 * @author jst
 * @since 24.07.2009
 */
@Singleton
public class BirtRuntimeTestHelper extends AbstractRuleTestHelper
{
  /** The BirtRuntimeManager */
  @Inject
  private IBirtRuntimeManager birtRuntimeManager;

  /**
   * Sets up the birt runtime environment
   * @throws Exception
   */
  @Override
  public void doSetUp() throws Exception
  {
    birtRuntimeManager.start(new NullProgressMonitor());
  }

  /**
   * Tears down the birt runtime environment
   * @throws Exception
   */
  @Override
  public void doTearDown() throws Exception
  {
    birtRuntimeManager.stop(new NullProgressMonitor());
  }

  /**
   * Returns the birtRuntimeManager
   * @return the birtRuntimeManager
   */
  public IBirtRuntimeManager getBirtRuntimeManager()
  {
    return birtRuntimeManager;
  }
}
