package ch.ivyteam.ivy.reporting.restricted;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * @author fs
 * @since 20.06.2011
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  ch.ivyteam.ivy.reporting.internal.EclipseCoreTestReportDataCollector.class,
  ch.ivyteam.ivy.reporting.restricted.EclipseCoreTestBirtEngineSimpleReportGeneration.class,
  ch.ivyteam.ivy.reporting.restricted.EclipseCoreTestBirtRuntimeManagerOsgi.class,
  ch.ivyteam.ivy.reporting.restricted.EclipseCoreTestReportingManager.class
  })
public class AllBirtRuntimeEclipseTests {}