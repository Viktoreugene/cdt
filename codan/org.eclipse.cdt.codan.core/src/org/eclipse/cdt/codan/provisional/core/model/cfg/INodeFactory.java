/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.provisional.core.model.cfg;

/**
 * Control Flow Graph Node factory
 */
public interface INodeFactory {
	IPlainNode createPlainNode();

	IJumpNode createJumpNode();

	IDecisionNode createDecisionNode();

	IConnectorNode createConnectorNode();

	IBranchNode createBranchNode(String label);

	IStartNode createStartNode();

	IExitNode createExitNode();
}
