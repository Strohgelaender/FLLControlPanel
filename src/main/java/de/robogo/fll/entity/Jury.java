package de.robogo.fll.entity;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class Jury implements Serializable {

	private JuryType juryType;
	private int num;
	private String room;

	public Jury() {
		//default constructor for serialization
	}

	public Jury(final JuryType juryType, final int num, final String room) {
		this.juryType = juryType;
		this.num = num;
		this.room = room;
	}

	public JuryType getJuryType() {
		return juryType;
	}

	public int getNum() {
		return num;
	}

	public String getRoom() {
		return room;
	}

	//setters for Object mapping
	public void setJuryType(final JuryType juryType) {
		this.juryType = juryType;
	}

	public void setNum(final int num) {
		this.num = num;
	}

	public void setRoom(final String room) {
		this.room = room;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Jury jury = (Jury) o;
		return num == jury.num &&
				juryType == jury.juryType &&
				Objects.equals(room, jury.room);
	}

	@Override
	public int hashCode() {
		return Objects.hash(juryType, num, room);
	}

	public enum JuryType {
		TestRound("TR", "Test Round"),
		RobotDesign("R", "Robot Design"),
		Teamwork("T", "Teamwork"),
		Research("F", "Research"),
		LiveChallenge("LC", "Live Challenge");

		private final String shortName;
		private final String longName;

		JuryType(String shortText, final String longName) {
			this.shortName = shortText;
			this.longName = longName;
		}

		public String getShortName() {
			return shortName;
		}

		public String getLongName() {
			return longName;
		}
	}
}
