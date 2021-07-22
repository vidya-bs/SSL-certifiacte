package com.itorix.apiwiz.design.studio.model;

public class EmptyXlsRows extends Exception {

	private static final long serialVersionUID = 1L;
	private int rowNum = 0;

	public EmptyXlsRows(int r) {
		rowNum = r;
	}

	@Override
	public String toString() {
		return ("Please delete empty Row's or Cell's at the Row Number ==" + rowNum).toString();
	}
}
