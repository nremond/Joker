package cl.own.usi.model;

import java.util.List;

public class Question {

	int number;
	
	String label;
	
	List<String> choices;
	
	int correctChoice;

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
	
	
}
