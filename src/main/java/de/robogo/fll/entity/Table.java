package de.robogo.fll.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class Table implements Serializable {

	private String name;

	public Table() {
		//default constructor for object mapping
	}

	public Table(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	//setter for object mapping
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
