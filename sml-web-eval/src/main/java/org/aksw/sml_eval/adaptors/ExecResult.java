package org.aksw.sml_eval.adaptors;

public class ExecResult {
	private String stdout;
	private String stderr;
	
	public ExecResult(String stdout, String stderr) {	
		super();
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public String getStdout() {
		return stdout;
	}

	public String getStderr() {
		return stderr;
	}
}
