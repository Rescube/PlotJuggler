
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
//import javax.swing.JOptionPane;

public class GdbExec {
	OutputStream m_stdin = null;
	InputStream m_stderr = null;
	BufferedInputStream m_stdout = null;
	Process m_p;
	boolean m_bIsRunning = false;
	boolean verbose = true;

	final int IO_ERROR = 0;
	final int CMD_ERROR = 1;
	final int CMD_IN_PROGRESS = 2;
	final int CMD_END_OK = 3;

	final String GDB_PROMPT = "(gdb) ";
	final int GDB_PROMPT_SIZE = 6;

	int m_status = 3;
	String m_stdoutStr = "";
	String m_stderrStr = "";
	long addressOffset = 0L;
	InfoVar m_getGdbVar;

	public GdbExec(long paramLong) {
		this.addressOffset = paramLong;
	}

	public GdbExec(String paramString, long paramLong) {
		int i = 3;
		if (!this.m_bIsRunning) {
			runGdb();
			if (this.m_bIsRunning == true) {
				i = sendCmdAndWaitAnswer("");

				if (i == 3) {
					i = sendCmdAndWaitAnswer("symbol-file \"" + paramString + "\"\n");
				}
				this.addressOffset = paramLong;
				this.verbose = true;
			}
		}
	}

	public boolean isRunning() {
		return this.m_bIsRunning;
	}

	public void runGdb() {
		try {
			this.m_p = Runtime.getRuntime().exec("/usr/bin/arm-none-eabi-gdb");
			this.m_bIsRunning = true;
		} catch (IOException localIOException) {
			System.out.println("Failed launching gdb.exe: " + localIOException.getMessage());
			// JOptionPane.showMessageDialog(null, "Failed launching gdb.exe: " +
			// localIOException.getMessage(), "Message", 0);

		}

		this.m_stdin = this.m_p.getOutputStream();
		this.m_stderr = this.m_p.getErrorStream();
		this.m_stdout = new BufferedInputStream(this.m_p.getInputStream());
	}

	public String readAnswer(InputStream paramInputStream, boolean paramBoolean) {
		String str = new String("");
		try {
			int i;
			if (paramBoolean == true) {
				do {
					i = paramInputStream.available();
					if (i == 0) {
						try {
							Thread.sleep(1L);
						} catch (InterruptedException localInterruptedException) {
							System.out.println("Thread wakes up by external: " + localInterruptedException.getMessage());
							// JOptionPane.showMessageDialog(null, "Thread wakes up by external: " +
							// localInterruptedException.getMessage(), "Message", 0);
						}
					}
				}

				while (i == 0);
			} else {
				i = paramInputStream.available();
			}
			while (i > 0) {
				byte[] arrayOfByte = new byte[i];
				paramInputStream.read(arrayOfByte, 0, i);
				str = str + new String(arrayOfByte);

				i = paramInputStream.available();
			}
		} catch (IOException localIOException) {
			this.m_status = 0;
			System.out.println("Failed getting gdb answer: " + localIOException.getMessage());
			// JOptionPane.showMessageDialog(null, "Failed getting gdb answer: " +
			// localIOException.getMessage(), "Message", 0);
		}

		return str;
	}

	private void sendCmd(String paramString) {
		try {
			this.m_stdin.write(paramString.getBytes());
			this.m_stdin.flush();
		} catch (IOException localIOException) {
			this.m_status = 0;
			System.out.println("Failed sending cmd " + paramString + ": " + localIOException.getMessage());
			// JOptionPane.showMessageDialog(null, "Failed sending cmd " + paramString + ":
			// " + localIOException.getMessage(), "Message", 0);
		}
	}

	public void stop() {
		try {
			this.m_stdin.close();
			this.m_stderr.close();
			this.m_stdout.close();
			this.m_p.destroy();
			this.m_bIsRunning = false;
		} catch (IOException localIOException) {
			// JOptionPane.showMessageDialog(null, "Failed closing stream: " +
			// localIOException.getMessage(), "Message", 0);
		}
	}

	public int sendCmdAndWaitAnswer(String paramString) {
		if (!paramString.isEmpty()) {
			sendCmd(paramString);
			if (this.m_status == 0) {
				return 0;
			}

		}

		this.m_status = 2;
		this.m_stdoutStr = "";

		while (this.m_status == 2) {
			this.m_stdoutStr += readAnswer(this.m_stdout, true);
			if (this.m_status == 0) {
				return 0;
			}
			if (this.m_stdoutStr.endsWith("(gdb) ")) {
				this.m_status = 3;
			} else {
				this.m_status = 2;
			}

			this.m_stderrStr = readAnswer(this.m_stderr, false);
			if (this.m_status == 0) {
				return 0;
			}
			if ((!this.m_stderrStr.isEmpty()) && (this.verbose)) {
				System.out.println("GDBS cmd [" + paramString.trim() + "] failed: " + this.m_stderrStr);
				// JOptionPane.showMessageDialog(null, "GDB cmd [" + paramString.trim() + "]
				// failed: " + this.m_stderrStr, "Message", 0);

				this.m_status = 1;
			}
		}
		return this.m_status;
	}

	public Iterator<GdbVar> getVarIterator() {
		return this.m_getGdbVar.getVarIterator();
	}

	public String getTypeDesc(String paramString) {
		int i = 3;
		String str = null;

		if (this.m_bIsRunning == true) {
			i = sendCmdAndWaitAnswer("ptype " + paramString + "\n");
			if (i == 3) {
				if ((this.m_stdoutStr.startsWith("type = ")) && (this.m_stdoutStr.endsWith("(gdb) "))) {
					str = new String(this.m_stdoutStr.substring(7, this.m_stdoutStr.length() - 6).trim());
				}
			}
		}
		return str;
	}

	public short getType(String paramString) {
		InfoVar localInfoVar = new InfoVar(this);
		return localInfoVar.getType(getTypeDesc(paramString), paramString);
	}

	public String getSizeof(String paramString) {
		int i = 3;
		String str = "";

		if (this.m_bIsRunning == true) {
			i = sendCmdAndWaitAnswer("p sizeof " + paramString + "\n");
			if (i == 3) {
				if ((this.m_stdoutStr.indexOf("=") != -1) && (this.m_stdoutStr.endsWith("(gdb) "))) {
					str = new String(this.m_stdoutStr
							.substring(this.m_stdoutStr.indexOf("=") + 1, this.m_stdoutStr.length() - 6).trim());
				}
			}
		}
		return str;
	}

	public boolean buildVarList(String paramString, boolean paramBoolean) {
		int i = 3;
		boolean bool = false;

		if (!this.m_bIsRunning) {
			runGdb();
			if (this.m_bIsRunning == true) {
				i = sendCmdAndWaitAnswer("");
			}
		}
		if (this.m_bIsRunning == true) {
			if (i == 3) {
				i = sendCmdAndWaitAnswer("symbol-file \"" + paramString + "\"\n");
			}
			if (i == 3) {
				i = sendCmdAndWaitAnswer("info variables\n");
				if (i == 3) {
					bool = true;

					this.m_getGdbVar = new InfoVar(this, this.m_stdoutStr);

					this.m_getGdbVar.Parse(paramBoolean);
				}
			}
			stop();
		}
		m_getGdbVar.dump();
		return bool;
	}

	public long getSymbolAddress(String paramString) {
		int i = 3;
		long l = -1L;

		i = sendCmdAndWaitAnswer("print /x &(" + paramString + ")\n");
		if (i == 3) {
			int j = 0;

			String str = "= ";
			int k = this.m_stdoutStr.indexOf(str);
			if (k != -1) {
				int m = k + str.length();

				int n = this.m_stdoutStr.indexOf("\n", m);
				if (n != -1) {
					l = this.addressOffset + Long.decode(this.m_stdoutStr.substring(m, n)).longValue();
					j = 1;
				}
			}
			if ((j == 0) && (this.verbose)) {
				System.out.println("Error while extracting address from string: " + this.m_stdoutStr + ": set to 0");
				// JOptionPane.showMessageDialog(null, "Error while extracting address from
				// string: " + this.m_stdoutStr + ": set to 0", "Message", 0);
			}

		}

		return l;
	}
}

/*
 * Location: /home/mm/STMStudio.jar Qualified Name: com.st.stmstudio.gdb.GdbExec
 * JD-Core Version: 0.6.2
 */
