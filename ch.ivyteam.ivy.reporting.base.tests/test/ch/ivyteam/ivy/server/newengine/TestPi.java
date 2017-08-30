package ch.ivyteam.ivy.server.newengine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.ivyteam.ivy.reporting.internal.BirtRuntimeManagerExternal;
import ch.ivyteam.ivy.server.AbstractProjectBasedServerTest;
import ch.ivyteam.util.Pair;
import mockit.Mock;
import mockit.MockUp;

@Ignore("to much ivy test environment involved -> easier and more effective to test in eclipse RCPTT")
public class TestPi extends AbstractProjectBasedServerTest
{
  private static final File TEST_PROJECT = new File("testProjects/testNewEngine");
  private String designPathFromMock;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    AbstractProjectBasedServerTest.setUp(TEST_PROJECT);
  }
  
  @Before
  @SuppressWarnings("unused")
  public void beforeTest()
  {
    new BirtServerMock();
  }

  @Test
  /*
   * Test the provided Birt extension {@link ch.ivyteam.ivy.reporting.extension.BirtReportGeneratorPI}
   * Mocked on server
   */
  public void testProvidedBirtExtension() throws Exception
  {
    String designName = "TechnicalReport.rptdesign";
    File designPath = new File(TEST_PROJECT, "/webContent/customFiles/" + designName);
    startProcessInNewEngineOverHttpWithBirtPathsProvided("providedBirtExtension", createUrlPath(designPath.toURI()));
    assertThat(designPathFromMock).endsWith(designName);
  }
  
  private String createUrlPath(URI uri)
  {
    return StringUtils.removeStart(uri.getPath(), "/");
  }
  
  private String startProcessInNewEngineOverHttpWithBirtPathsProvided(String processName, String birtDesignPath) throws Exception, IOException
  {
    @SuppressWarnings("unchecked")
    Pair<String, String>[] startParameters = new Pair[]{new Pair<String, String>("designPath", birtDesignPath)};
    URI processStartUri = processStartHelper.getProcessRequestUri(getTestProcessModelVersion(), processName, startParameters);
    return processStartHelper.executeHttpRequestAndGetContent(processStartUri);
  }
  
  public class BirtServerMock extends MockUp<BirtRuntimeManagerExternal>
  {
    @SuppressWarnings("unused")
    @Mock
    public void createSimpleReport(String designPath, List<String> formats, File reportPath, String reportName, Map<String, Object> parameterMap)
    {
      designPathFromMock = designPath;
    }
  }

}
