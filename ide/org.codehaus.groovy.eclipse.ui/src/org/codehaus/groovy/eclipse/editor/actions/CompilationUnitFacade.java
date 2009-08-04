/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * @author Andrew Eisenberg
 * @created May 26, 2009
 *
 */
public class CompilationUnitFacade implements IDocumentFacade {

    private final ICompilationUnit unit;
    private final IDocument document;
    
    public CompilationUnitFacade(ICompilationUnit unit) {
        this.unit = unit;
        this.document = new Document(new String(((CompilationUnit) unit).getContents()));
    }
    
    public IDocument getDocument() {
        return document;
    }

    public IFile getFile() {
        return (IFile) unit.getResource();
    }

    public String getLine(int row) throws BadLocationException {
        IRegion r = document.getLineInformation(row);
        return document.get(r.getOffset(), r.getLength());
    }

    public int getLineLength(int row) throws BadLocationException {
        IRegion r = document.getLineInformation(row);
        return r.getLength();
    }

    public int getLineOffset(int row) throws BadLocationException {
        IRegion r = document.getLineInformation(row);
        return r.getOffset();
    }

    public List<String> getLines(int startRow, int endRow)
            throws BadLocationException {
        List<String> lines = new ArrayList<String>(endRow-startRow);
        for (int i = startRow; i < endRow; i++) {
            lines.add(getLine(i));
        }
        return lines;
    }

    public int getOffset(int row, int col) throws BadLocationException {
        int offset = getLineOffset(row);
        return offset + col;
    }

//    public Point getRowCol(int offset) throws BadLocationException {
//        int row = document.getLineOfOffset(offset)+1;
//        IRegion r = document.getLineInformationOfOffset(offset);
//        int col = offset - r.getOffset();
//        return new Point(col, row);
//    }

    public String getText(int offset, int len) throws BadLocationException {
        return document.get(offset, len);
    }

    public ModuleNode getModuleNode() {
        return unit instanceof GroovyCompilationUnit ? ((GroovyCompilationUnit) unit).getModuleNode() : null;
    }
    
    /**
     * expands the given region so that it covers the java identifier currently selected
     */
    public IRegion expandRegion(IRegion region) {
        int start = region.getOffset();
        int end = start + region.getLength();
        int docEnd = document.getLength();
        try {
            start--;
            while (start > 0 && Character.isJavaIdentifierPart(document.getChar(start))) {
                start--;
            }
            start++;
            while (end < docEnd && Character.isJavaIdentifierPart(document.getChar(end))) {
                end++;
            }
            return new Region(start, end-start);
        } catch (BadLocationException e) {
            GroovyCore.logException("Exception while expanding location", e);
        }
        return region;
    }

    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        return (T) unit.getAdapter(type);
    }

    public GroovyProjectFacade getProjectFacade() {
        return new GroovyProjectFacade(unit);
    }
}
