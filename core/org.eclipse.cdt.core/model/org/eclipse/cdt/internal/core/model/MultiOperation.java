package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;

/**
 * This class is used to perform operations on multiple <code>ICElement</code>.
 * It is responible for running each operation in turn, collecting
 * the errors and merging the corresponding <code>CElementDelta</code>s.
 * <p>
 * If several errors occured, they are collected in a multi-status
 * <code>CModelStatus</code>. Otherwise, a simple <code>CModelStatus</code>
 * is thrown.
 */
public abstract class MultiOperation extends CModelOperation {
	/**
	 * The list of renamings supplied to the operation
	 */
	protected String[] fRenamingsList= null;

	/**
	 * Table specifying the new parent for elements being 
	 * copied/moved/renamed.
	 * Keyed by elements being processed, and
	 * values are the corresponding destination parent.
	 */
	protected Map fParentElements;

	/**
	 * Table specifying insertion positions for elements being 
	 * copied/moved/renamed. Keyed by elements being processed, and
	 * values are the corresponding insertion point.
	 * @see processElements(IProgressMonitor)
	 */
	protected Map fInsertBeforeElements= new HashMap(1);

	/**
	 * This table presents the data in <code>fRenamingList</code> in a more
	 * convenient way.
	 */
	protected Map fRenamings;

	/**
	 * Creates a new <code>MultiOperation</code>.
	 */
	protected MultiOperation(ICElement[] elementsToProcess, ICElement[] parentElements, boolean force) {
		super(elementsToProcess, parentElements, force);
		fParentElements = new HashMap(elementsToProcess.length);
		if (elementsToProcess.length == parentElements.length) {
			for (int i = 0; i < elementsToProcess.length; i++) {
				fParentElements.put(elementsToProcess[i], parentElements[i]);
			}
		} else { //same destination for all elements to be moved/copied/renamed
			for (int i = 0; i < elementsToProcess.length; i++) {
				fParentElements.put(elementsToProcess[i], parentElements[0]);
			}
		}
	}

	/**
	 * Creates a new <code>MultiOperation</code> on <code>elementsToProcess</code>.
	 */
	protected MultiOperation(ICElement[] elementsToProcess, boolean force) {
		super(elementsToProcess, force);
	}

	/**
	 * Convenience method to create a <code>CModelException</code>
	 * embending a <code>CModelStatus</code>.
	 */
	protected void error(int code, ICElement element) throws CModelException {
		throw new CModelException(new CModelStatus(code, element));
	}

	/**
	 * Executes the operation.
	 *
	 * @exception CModelException if one or several errors occured during the operation.
	 * If multiple errors occured, the corresponding <code>CModelStatus</code> is a
	 * multi-status. Otherwise, it is a simple one.
	 */
	protected void executeOperation() throws CModelException {
		try {
			processElements();
		} catch (CModelException cme) {
			throw cme;
		} finally {
			mergeDeltas();
		}
	}

	/**
	 * Returns the parent of the element being copied/moved/renamed.
	 */
	protected ICElement getDestinationParent(ICElement child) {
		return (ICElement)fParentElements.get(child);
	}

	/**
	 * Returns the name to be used by the progress monitor.
	 */
	protected abstract String getMainTaskName();

	/**
	 * Returns the new name for <code>element</code>, or <code>null</code>
	 * if there are no renamings specified.
	 */
	protected String getNewNameFor(ICElement element) {
		if (fRenamings != null)
			return (String) fRenamings.get(element);
		else
			return null;
	}

	/**
	 * Sets up the renamings hashtable - keys are the elements and
	 * values are the new name.
	 */
	private void initializeRenamings() {
		if (fRenamingsList != null && fRenamingsList.length == fElementsToProcess.length) {
			fRenamings = new HashMap(fRenamingsList.length);
			for (int i = 0; i < fRenamingsList.length; i++) {
				if (fRenamingsList[i] != null) {
					fRenamings.put(fElementsToProcess[i], fRenamingsList[i]);
				}
			}
		}
	}

	/**
	 * Returns <code>true</code> if this operation represents a move or rename, <code>false</code>
	 * if this operation represents a copy.<br>
	 * Note: a rename is just a move within the same parent with a name change.
	 */
	protected boolean isMove() {
		return false;
	}

	/**
	 * Returns <code>true</code> if this operation represents a rename, <code>false</code>
	 * if this operation represents a copy or move.
	 */
	protected boolean isRename() {
		return false;
	}

	/**
	 * Process all of the changed deltas generated by these operations.
	 */
	protected void mergeDeltas() {
		if (fDeltas != null) {
			CElementDelta rootDelta = newCElementDelta();
			boolean insertedTree = false;
			for (int i = 0; i < fDeltas.length; i++) {
				ICElementDelta delta = fDeltas[i];
				ICElementDelta[] children = delta.getAffectedChildren();
				for (int j = 0; j < children.length; j++) {
					CElementDelta projectDelta = (CElementDelta) children[j];
					rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
					insertedTree = true;
				}
			}
			if (insertedTree)
				fDeltas = new ICElementDelta[] {rootDelta};
			else
				fDeltas = null;
		}
	}

	/**
	 * Subclasses must implement this method to process a given <code>ICElement</code>.
	 */
	protected abstract void processElement(ICElement element) throws CModelException;

	/**
	 * Processes all the <code>ICElement</code>s in turn, collecting errors
	 * and updating the progress monitor.
	 *
	 * @exception CModelException if one or several operation(s) was unable to
	 * be completed.
	 */
	protected void processElements() throws CModelException {
		beginTask(getMainTaskName(), fElementsToProcess.length);
		ICModelStatus[] errors = new ICModelStatus[3];
		int errorsCounter = 0;
		for (int i = 0; i < fElementsToProcess.length; i++) {
			try {
				verify(fElementsToProcess[i]);
				processElement(fElementsToProcess[i]);
			} catch (CModelException jme) {
				if (errorsCounter == errors.length) {
					// resize
					System.arraycopy(errors, 0, (errors = new ICModelStatus[errorsCounter*2]), 0, errorsCounter);
				}
				errors[errorsCounter++] = jme.getCModelStatus();
			} finally {
				worked(1);
			}
		}
		done();
		if (errorsCounter == 1) {
			throw new CModelException(errors[0]);
		} else if (errorsCounter > 1) {
			if (errorsCounter != errors.length) {
				// resize
				System.arraycopy(errors, 0, (errors = new ICModelStatus[errorsCounter]), 0, errorsCounter);
			}
			throw new CModelException(CModelStatus.newMultiStatus(errors));
			}
	}

	/**
	 * Sets the insertion position in the new container for the modified element. The element
	 * being modified will be inserted before the specified new sibling. The given sibling
	 * must be a child of the destination container specified for the modified element.
	 * The default is <code>null</code>, which indicates that the element is to be
	 * inserted at the end of the container.
	 */
	public void setInsertBefore(ICElement modifiedElement, ICElement newSibling) {
		fInsertBeforeElements.put(modifiedElement, newSibling);
	}

	/**
	 * Sets the new names to use for each element being copied. The renamings
	 * correspond to the elements being processed, and the number of
	 * renamings must match the number of elements being processed.
	 * A <code>null</code> entry in the list indicates that an element
	 * is not to be renamed.
	 *
	 * <p>Note that some renamings may not be used.  If both a parent
	 * and a child have been selected for copy/move, only the parent
	 * is changed.  Therefore, if a new name is specified for the child,
	 * the child's name will not be changed.
	 */
	public void setRenamings(String[] renamings) {
		fRenamingsList = renamings;
		initializeRenamings();
	}

	/**
	 * This method is called for each <code>ICElement</code> before
	 * <code>processElement</code>. It should check that this <code>element</code>
	 * can be processed.
	 */
	protected abstract void verify(ICElement element) throws CModelException;

	/**
	 * Verifies that the <code>destination</code> specified for the <code>element</code> is valid for the types of the
	 * <code>element</code> and <code>destination</code>.
	 */
	protected void verifyDestination(ICElement element, ICElement destination) throws CModelException {
		if (destination == null || !destination.exists())
			error(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, destination);
	
	}

	/**
	 * Verify that the new name specified for <code>element</code> is
	 * valid for that type of C element.
	 */
	protected void verifyRenaming(ICElement element) throws CModelException {
		String newName = getNewNameFor(element);
		boolean isValid = true;
		// Validate the name here.
		if (newName.indexOf(' ') != -1) {
			isValid = false;
		}

		if (!isValid) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.INVALID_NAME, element, newName));
		}
	}

	/**
	 * Verifies that the positioning sibling specified for the <code>element</code> is exists and
	 * its parent is the destination container of this <code>element</code>.
	 */
	protected void verifySibling(ICElement element, ICElement destination) throws CModelException {
		ICElement insertBeforeElement = (ICElement) fInsertBeforeElements.get(element);
		if (insertBeforeElement != null) {
			if (!insertBeforeElement.exists() || !insertBeforeElement.getParent().equals(destination)) {
				error(ICModelStatusConstants.INVALID_SIBLING, insertBeforeElement);
			}
		}
	}
}
