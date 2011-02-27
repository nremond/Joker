package cl.own.usi.model;

import java.util.List;

/**
 * Question representation
 * 
 * @author bperroud
 *
 */
public class Question {

	private int number;

	private String label;

	private List<String> choices;

	private int correctChoice;

	private int value = 0;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getChoices() {
		return choices;
	}

	public void setChoices(List<String> choices) {
		this.choices = choices;
	}

	public int getCorrectChoice() {
		return correctChoice;
	}

	public void setCorrectChoice(int correctChoice) {
		this.correctChoice = correctChoice;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
