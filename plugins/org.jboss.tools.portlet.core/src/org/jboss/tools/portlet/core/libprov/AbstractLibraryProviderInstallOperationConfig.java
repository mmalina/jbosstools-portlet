package org.jboss.tools.portlet.core.libprov;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderInstallOperationConfig;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectBase;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.jboss.tools.portlet.core.IPortletConstants;
import org.jboss.tools.portlet.core.Messages;
import org.jboss.tools.portlet.core.PortletCoreActivator;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public abstract class AbstractLibraryProviderInstallOperationConfig extends
		LibraryProviderInstallOperationConfig {

	private boolean addRichfacesCapabilities;
	private String richfacesType;
	private String richfacesRuntime;
	
	@Override
	public synchronized IStatus validate() {
		IStatus status = super.validate();
		if (!status.isOK()) {
			return status;
		}
		if (!addRichfacesCapabilities) {
			return status;
		}
		if (!IPortletConstants.LIBRARIES_PROVIDED_BY_RICHFACES.equals(richfacesType)) {
			return status;
		}
		if (richfacesRuntime == null) {
			return getInvalidRichfacesRuntime();
		}
		richfacesRuntime = richfacesRuntime.trim();
		if (richfacesRuntime.length() <= 0) {
			return getInvalidRichfacesRuntime();
		}
		File folder = new File(richfacesRuntime);
		if (!folder.exists() || !folder.isDirectory()) {
			return getInvalidRichfacesRuntime();
		}
		folder = new File(folder,"lib"); //$NON-NLS-1$
		if (!folder.exists() || !folder.isDirectory()) {
			return getInvalidRichfacesRuntime();
		}
		String[] fileList = folder.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (name.startsWith("richfaces") || name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
				return false;
			}

		});
		if (fileList.length < 3) {
			return getInvalidRichfacesRuntime();
		}
		
		return Status.OK_STATUS;
	}

	private IStatus getInvalidRichfacesRuntime() {
		IStatus status = new Status( IStatus.ERROR, PortletCoreActivator.PLUGIN_ID, Messages.PortletPostInstallListener_Invalid_Richfaces_Runtime );
		return status;
	}
	
	private void updatePreferences() {
		IProjectFacet f = getProjectFacet();
        try {
			Preferences prefs = FacetedProjectFramework.getPreferences( f );
			prefs = prefs.node(IPortletConstants.PORTLET_BRIDGE_HOME);
			prefs.putBoolean(IPortletConstants.RICHFACES_CAPABILITIES, addRichfacesCapabilities);
			prefs.put(IPortletConstants.RICHFACES_LIBRARIES_TYPE, richfacesType);
			prefs.put(IPortletConstants.RICHFACES_RUNTIME, richfacesRuntime);
		} catch (BackingStoreException e) {
			PortletCoreActivator.log(e);
		}
	}
	
	public boolean isAddRichfacesCapabilities() {
		return addRichfacesCapabilities;
	}
	
	@Override
	public void init(IFacetedProjectBase fpj, IProjectFacetVersion fv,
			ILibraryProvider provider) {
		super.init(fpj, fv, provider);
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		IProjectFacet f = getProjectFacet();
        try {
			Preferences prefs = FacetedProjectFramework.getPreferences( f );
			prefs = prefs.node(IPortletConstants.PORTLET_BRIDGE_HOME);
			if( prefs.nodeExists( IPortletConstants.PREFS_PORTLETBRIDGE_HOME ) ) {
				addRichfacesCapabilities = prefs.getBoolean(IPortletConstants.RICHFACES_CAPABILITIES, false);
				richfacesType = prefs.get(IPortletConstants.RICHFACES_LIBRARIES_TYPE, null);
				richfacesRuntime = prefs.get(IPortletConstants.RICHFACES_RUNTIME, null);
			}
		} catch (BackingStoreException e) {
			PortletCoreActivator.log(e);
		}
	}
	
	public void setAddRichfacesCapabilities(boolean addRichfacesCapabilities) {
		boolean oldValue = this.addRichfacesCapabilities;
		this.addRichfacesCapabilities = addRichfacesCapabilities;
		notifyListeners(IPortletConstants.PORTLETBRIDGE_HOME, oldValue, addRichfacesCapabilities);
		updatePreferences();
	}
	public String getRichfacesType() {
		return richfacesType;
	}
	public void setRichfacesType(String richfacesType) {
		String oldValue = this.richfacesType;
		this.richfacesType = richfacesType;
		notifyListeners(IPortletConstants.PORTLETBRIDGE_HOME, oldValue, richfacesType);
		updatePreferences();
	}
	public String getRichfacesRuntime() {
		return richfacesRuntime;
	}
	public void setRichfacesRuntime(String richfacesRuntime) {
		String oldValue = this.richfacesRuntime;
		this.richfacesRuntime = richfacesRuntime;
		notifyListeners(IPortletConstants.PORTLETBRIDGE_HOME, oldValue, richfacesRuntime);
		updatePreferences();
	}
}