package ch.ivyteam.ivy.reporting.internal.dataset;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.reporting.internal.ReportingManager;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.ProjectReportDataEntry;
import ch.ivyteam.ivy.reporting.internal.dataset.entry.RoleReportDataEntry;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;
import ch.ivyteam.ivy.role.IRole;

/**
 * Scripted Data Set for Roles.
 * 
 * The Reports Scripted Data Set gets its Data from this Class. The DataSet needs to have its Event Handler
 * set to this Class. Upon opening of a Scripted data set the open method is called. To fetch one entry the
 * fetch method is called. To close the data source, close will be called.
 * 
 * @author jst
 * @since 19.06.2009
 */
public class RoleDataSetEventAdapter extends ReportingDataSetEventAdapter
{
  /** All roles. */
  private Iterator<RoleReportDataEntry> roleIt;

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#open(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void open(IDataSetInstance dataSet)
  {
    try
    {
      // Get the Role for the current Project
      String parentVersionName = (String) dataSet.getInputParameterValue("parentVersionName");
      for (ProjectReportDataEntry proj : reportDataCollector.getProjectReportData())
      {
        if (proj.getProcessModelVersion().getVersionName().equals(parentVersionName))
        {
          IRole root = proj.getIvyProject().getRootRole(null);
          roleIt = getFlatenedRoleHierarchy(root).iterator();
          break;
        }
      }
    }
    catch (ScriptException ex)
    {
      ReportingManager.getLogger().error("Error filling Report Data.", ex);
      throw new RuntimeException("Error filling Report Data.", ex);
    }
    catch (PersistencyException ex)
    {
      ReportingManager.getLogger().error("Error getting Report Data.", ex);
      throw new RuntimeException("Error getting Report Data.", ex);
    }
    catch (ResourceDataModelException ex)
    {
      ReportingManager.getLogger().error("Error getting Report Data.", ex);
      throw new RuntimeException("Error getting Report Data.", ex);
    }
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#fetch(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
   *      org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow)
   */
  @Override
  public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row)
  {

    /* Fill the data set. */
    if (roleIt != null && roleIt.hasNext())
    {
      try
      {
        RoleReportDataEntry entry = roleIt.next();
        row.setColumnValue("RoleId", entry.getId());
        row.setColumnValue("RoleName", entry.getName());
        row.setColumnValue("ParentRoleId", entry.getParentId());
      }
      catch (ScriptException ex)
      {
        ex.printStackTrace();
      }
      return true;
    }
    return false;
  }

  /**
   * @see org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter#close(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance)
   */
  @Override
  public void close(IDataSetInstance _dataSet)
  {
    roleIt = null;
  }

  /**
   * Recursivly returns the hierararchy of roles as a flatened list.
   * 
   * @param root A role to get all subroles of.
   * @return A list with all subroles (as RoleReportDataEntry) of the given role.
   */
  private List<RoleReportDataEntry> getFlatenedRoleHierarchy(IRole root)
  {
    List<RoleReportDataEntry> roles = new LinkedList<RoleReportDataEntry>();
    String parentId = (root.getParentRole() != null) ? root.getParentRole().getIdentifier() : "";
    roles.add(new RoleReportDataEntry(root.getIdentifier(), root.getDisplayName(), parentId));
    for (IRole role : root.getSubRoles())
    {
      roles.addAll(getFlatenedRoleHierarchy(role));
    }
    return roles;
  }
}
