/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.preview.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import com.aptana.preview.PreviewPlugin;
import com.aptana.preview.IPreviewHandler;

/**
 * @author Max Stepanov
 * 
 */
public final class PreviewHandlers {

	private static final String EXTENSION_POINT_ID = PreviewPlugin.PLUGIN_ID + ".previewHandlers"; //$NON-NLS-1$
	private static final String TAG_HANDLER = "handler"; //$NON-NLS-1$
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
	private static final String ATT_CONTENTTYPE = "contentType"; //$NON-NLS-1$

	private static PreviewHandlers instance = null;
	private Map<IContentType, IConfigurationElement> configurations = new HashMap<IContentType, IConfigurationElement>();

	/**
	 * 
	 */
	private PreviewHandlers() {
		readExtensionRegistry();
	}

	public static PreviewHandlers getInstance() {
		if (instance == null) {
			instance = new PreviewHandlers();
		}
		return instance;
	}

	private void readExtensionRegistry() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_ID);
		for (int i = 0; i < elements.length; ++i) {
			readElement(elements[i], TAG_HANDLER);
		}
	}

	private void readElement(IConfigurationElement element, String elementName) {
		if (!elementName.equals(element.getName())) {
			return;
		}
		if (TAG_HANDLER.equals(element.getName())) {
			String clazz = element.getAttribute(ATT_CLASS);
			if (clazz == null || clazz.length() == 0) {
				return;
			}
			String contentTypeId = element.getAttribute(ATT_CONTENTTYPE);
			if (contentTypeId == null || contentTypeId.length() == 0) {
				return;
			}
			IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
			if (contentType == null) {
				return;
			}
			configurations.put(contentType, element);
		}
	}

	public IPreviewHandler getHandler(IContentType contentType) throws CoreException {
		IConfigurationElement element = null;
		while (contentType != null && element == null) {
			element = configurations.get(contentType);
			contentType = contentType.getBaseType();
		}
		if (element != null) {
			return (IPreviewHandler) element.createExecutableExtension(ATT_CLASS);
		}
		return null;
	}

}
