/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Helper class implementation of the {@link IChildrenUpdate} update object.
 * 
 * @see VMViewerUpdate
 */
@SuppressWarnings("restriction")
public class VMChildrenUpdate extends VMViewerUpdate implements IChildrenUpdate {
    private final int fOffset;
    private final int fLength;
    protected final List<Object> fElements;
    
    public VMChildrenUpdate(IViewerUpdate clientUpdate, int offset, int length, 
        DataRequestMonitor<List<Object>> requestMonitor) 
    {
        super(clientUpdate, requestMonitor);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    public VMChildrenUpdate(IModelDelta delta, IPresentationContext presentationContext, int offset, int length, 
        DataRequestMonitor<List<Object>> rm) 
    {
        super(delta, presentationContext, rm);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    public VMChildrenUpdate(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, 
        int offset, int length, DataRequestMonitor<List<Object>> rm) 
    {
        super(elementPath, viewerInput, presentationContext, rm);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    public int getOffset() {
        return fOffset;
    }

    public int getLength() {
        return fLength;
    }

    public void setChild(Object element, int offset) {
        // Calculate the index in array based on configured offset.
        int idx = offset - (fOffset > 0 ? fOffset : 0);
        
        // To make sure that index is in valid range.
        if (idx < 0 || (fLength > 0 && idx >= fLength)) return;
        
        // Increase the list size if needed.
        ensureElementsSize(idx + 1);
        
        // Finally set the element in elements list.
        fElements.set(idx, element);
    }

    private void ensureElementsSize(int size) {
        while (fElements.size() < size) {
            fElements.add(null);
        }
    }
        
    @Override
    public String toString() {
        return "VMElementsUpdate for elements under parent = " + getElement() + ", in range " + getOffset() + " -> " + (getOffset() + getLength());  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
    }

    @Override
    public void done() {
        @SuppressWarnings("unchecked")
        DataRequestMonitor<List<Object>> rm = (DataRequestMonitor<List<Object>>)getRequestMonitor();
        
        /* See https://bugs.eclipse.org/bugs/show_bug.cgi?id=202109
         * 
         * A flexible hierarchy bug/optimization causes query with incorrect
         * IChildrenUpdate[] array length.
         *
         * We found this while deleting a register node. Example:
         * 
         *  the register view displays:
         *     PC
         *     EAX
         *     EBX
         *     ECX
         *     EDX
         * 
         *   we delete EBX and force a context refresh.
         * 
         *   flexible hierarchy queries for IChildrenUpdate[5] and IChildrenCountUpdate at
         * the same time.
         * 
         *   VMElementsUpdate, used by VMCache to wrap the IChildrenUpdate, generates an
         * IStatus.ERROR with message "Incomplete elements of updates" when fElements
         * count (provided by service) does not match the length provided by the original
         * update query.
         * 
         * Workaround, always set the elements array in the request monitor, but still set 
         * the error status. 
         */        
        rm.setData(fElements);
        if (rm.isSuccess() && fLength != -1 && fElements.size() != fLength) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Incomplete elements of updates", null)); //$NON-NLS-1$
        }
        super.done();
    }
    
}
