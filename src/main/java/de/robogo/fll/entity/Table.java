package de.robogo.fll.entity;

public class Table {

	private final String name;

	public Table(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
