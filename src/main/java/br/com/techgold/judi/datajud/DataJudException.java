package br.com.techgold.judi.datajud;

public class DataJudException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataJudException(String message) {
		super(message);
	}

	public DataJudException(String message, Throwable cause) {
		super(message, cause);
	}

}
