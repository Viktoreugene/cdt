-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2008 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------


$Notice
-- Copied into all files generated by LPG
/./*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *********************************************************************************/
 
 // This file was generated by LPG
./
$End


$Define
	-- These macros allow the template and header code to be customized by an extending parser.
	$ast_class /.Object./
	$data_class /. Object ./ -- allow anything to be passed between actions
	
	$extra_interfaces /. ./
	$additional_interfaces /. , IParserActionTokenProvider, IParser $extra_interfaces ./
	
	$build_action_class /.  ./
	$resolve_action_class /.  ./
	$node_factory_create_expression /.  ./
	
	$lexer_class /.  ./
	$action_class /.  ./
	
	$Build /. $BeginAction action. ./
	$EndBuild /. $EndAction ./
$End



$Globals
/.	
	import java.util.*;
	import org.eclipse.cdt.core.dom.ast.*;
	import org.eclipse.cdt.core.dom.lrparser.IParser;
	import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
	import org.eclipse.cdt.core.dom.lrparser.lpgextensions.FixedBacktrackingParser;
./
$End

$Headers
/.
	private $build_action_class action;
	
	public $action_type() {  // constructor
	}
	
	private void initActions(IASTTranslationUnit tu) {
		action = new $build_action_class($node_factory_create_expression, this, tu);
	}
	
	
	public void addToken(IToken token) {
		token.setKind(mapKind(token.getKind())); // TODO does mapKind need to be called?
		super.addToken(token);
	}
	
	
	public IASTCompletionNode parse(IASTTranslationUnit tu) {
		// this has to be done, or... kaboom!
		setStreamLength(getSize());
		initActions(tu);
		
		final int errorRepairCount = -1;  // -1 means full error handling
		parser(null, errorRepairCount); // do the actual parse
		super.resetTokenStream(); // allow tokens to be garbage collected
	
		// the completion node may be null
		IASTCompletionNode compNode = action.getASTCompletionNode();
	
		//action = null;
		//parserAction = null;
		return compNode;
	}

	// uncomment this method to use with backtracking parser
	public List getRuleTokens() {
	    return Collections.unmodifiableList(getTokens().subList(getLeftSpan(), getRightSpan() + 1));
	}
	
	
	public IASTNode getSecondaryParseResult() {
		return  action.getSecondaryParseResult();
	}
	
	public String[] getOrderedTerminalSymbols() {
		return $sym_type.orderedTerminalSymbols;
	}
	
	public String getName() {
		return "$action_type"; //$NON-NLS-1$
	}
	
./
$End

$Globals
/.
    import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
    import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
./
$End

$Headers
/.

	private ITokenMap tokenMap = null;
	
	public void setTokens(List<IToken> tokens) {
		resetTokenStream();
		addToken(new Token(null, 0, 0, 0)); // dummy token
		for(IToken token : tokens) {
			token.setKind(tokenMap.mapKind(token.getKind()));
			addToken(token);
		}
		addToken(new Token(null, 0, 0, $sym_type.TK_EOF_TOKEN));
	}
	
	public $action_type(String[] mapFrom) {  // constructor
		tokenMap = new TokenMap($sym_type.orderedTerminalSymbols, mapFrom);
	}	
	

./
$End
	
