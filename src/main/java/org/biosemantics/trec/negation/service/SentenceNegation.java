package org.biosemantics.trec.negation.service;

public class SentenceNegation {
	private int start_pos = -1;
	private int end_pos = -1;
	private String phrase = "-1";

	public String getPhrase() {
		return phrase;
	}
	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public int getStart_pos() {
		return start_pos;
	}
	public void setStart_pos(int start_pos) {
		this.start_pos = start_pos;
	}
	public int getEnd_pos() {
		return end_pos;
	}
	public void setEnd_pos(int end_pos) {
		this.end_pos = end_pos;
	}

}