package cl.own.usi.model;

import java.io.Serializable;
import java.util.List;

/**
 * Question representation
 * 
 * @author bperroud
 *
 */
public class Question implements Serializable {

	private static final long serialVersionUID = 1L;

	private int number;

	private String label;

	private List<String> choices;

	private int correctChoiceNumber;

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

	public int getCorrectChoiceNumber() {
		return correctChoiceNumber;
	}
	
	private String correctChoice;
	public String getCorrectChoice() {
		return correctChoice;
	}

	public void setCorrectChoice(int correctChoiceNumber) {
		this.correctChoiceNumber = correctChoiceNumber;
		this.correctChoice = getChoices().get(correctChoiceNumber - 1);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
