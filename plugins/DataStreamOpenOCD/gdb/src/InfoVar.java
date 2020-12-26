
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InfoVar
{
	private String m_infoVarStr;
	private List<GdbVar> m_varList;
	private GdbExec m_gdb;
	private String FILE_MARKER_START = "File ";
	private String FILE_MARKER_END = ":\n";
	private String NON_DEBUG_MARKER = "Non-debugging symbols:";

	InfoVar(GdbExec paramGdbExec) {
		this.m_gdb = paramGdbExec;
	}

	InfoVar(GdbExec paramGdbExec, String paramString) {
		this.m_infoVarStr = paramString;
		this.m_varList = new ArrayList();
		this.m_gdb = paramGdbExec;
	}
	public void dump()
	{
		for (Object localObject = getVarIterator(); ((Iterator)localObject).hasNext(); ) {
			GdbVar localGdbVar = (GdbVar)((Iterator)localObject).next();
			localGdbVar.dump();
		}
	}

	public void Parse(boolean paramBoolean)
	{
		this.m_varList.clear();

		if (this.m_infoVarStr.isEmpty()) {
			return;
		}

		int i = this.m_infoVarStr.indexOf(this.FILE_MARKER_START);

		int k = 0;

		while ((i != -1) && (k == 0))
		{
			int j = this.m_infoVarStr.indexOf(this.FILE_MARKER_START, i + 1);
			if (j == -1)
			{
				j = this.m_infoVarStr.indexOf(this.NON_DEBUG_MARKER, i + 1);
				k = 1;
			}
			if (j == -1)
			{
				j = this.m_infoVarStr.length() - 1;
				k = 1;
			}

			int m = this.m_infoVarStr.indexOf(this.FILE_MARKER_END, i);
			if (m == -1) {
				System.out.println("Info var parsing error while getting filename");
				return;
			}
			String str = this.m_infoVarStr.substring(i + this.FILE_MARKER_START.length(), m);

			BuildVarList(str, this.m_infoVarStr.substring(m + this.FILE_MARKER_END.length(), j), "", paramBoolean);

			i = j;
		}
	}

	private int GetMatchingClosingBrace(String paramString)
	{
		int i = paramString.indexOf("{");
		if (i == -1) {
			return 0;
		}

		int j = 1;
		int k = paramString.indexOf("{", i + 1);
		int m = paramString.indexOf("}", i + 1);
		while (j != 0) {
			if (m == -1)
			{
				return -1;
			}
			if (k != -1)
			{
				if (k < m)
				{
					j++;
					k = paramString.indexOf("{", k + 1);
				}
			}

			j--;
			if (j > 0)
			{
				m = paramString.indexOf("}", m + 1);
			}
		}
		return m;
	}

	private void BuildVarList(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
	{
		int i = 0;

		while (i < paramString2.length()) {
			String str2 = paramString2.substring(i);
			int j = str2.indexOf(";");
			int k = str2.indexOf("{");
			int m = 0;
			if ((k != -1) && (k < j))
			{
				m = GetMatchingClosingBrace(str2);
				if (m == -1)
				{
					System.out.println("Info var parsing error: opened brace not closed");
					return;
				}
			}

			int n = str2.indexOf(";", m);
			if (n == -1)
				return;
			String str1;
			if (m != 0) {
				str1 = str2.substring(0, k) + str2.substring(m + 1, n);
			}
			else {
				str1 = str2.substring(0, n);
			}
			ExtractIdentifier(paramString1, str1, paramString3, paramBoolean);
			i += n + 1;
		}
	}

	private void ExtractIdentifier(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
	{
		if (paramString2.contains("lastAcked")) {
			System.out.print("last");
		}
		String str1 = paramString2.trim();

		int j = str1.indexOf("[");
		String str2;
		String str4;
		if (j != -1)
		{
			if (j != str1.lastIndexOf("["))
			{
				return;
			}
			String str5 = str1.substring(str1.lastIndexOf(" ", j) + 1, j).trim();

			if (str5.lastIndexOf("*") != -1)
				str5 = str5.substring(str5.lastIndexOf("*") + 1).trim();
			int i;
			if (paramBoolean == true)
			{
				String str6 = str1.substring(j + 1, str1.indexOf("]", j));
				i = Integer.parseInt(str6);
			}
			else
			{
				i = 1;
			}

			str2 = paramString3 + str5 + "[0]";
			str4 = this.m_gdb.getTypeDesc(str2);
			for (int m = 0; m < i; m++)
			{
				str2 = paramString3 + str5 + "[" + m + "]";
				if (str4 != null)
					ExtractType(paramString1, str4, str2, paramBoolean);
			}
			return;
		}
		String str3;
		if (str1.lastIndexOf("*") != -1)
		{
			int k = str1.indexOf(")");
			if (k != -1)
			{
				str1 = str1.substring(0, k);
			}

			str1 = str1.substring(str1.lastIndexOf("*") + 1).trim();

			if (str1.lastIndexOf(" ") != -1) {
				str1 = str1.substring(str1.lastIndexOf(" ") + 1).trim();
			}
			if (str1.isEmpty())
			{
				return;
			}
			str2 = paramString3 + str1;
			str3 = str1;
		}
		else if (str1.contains("::"))
		{
			if (str1.contains("<"))
			{
				return;
			}
			if ((str1.startsWith("const ")) || (str1.startsWith("static const ")))
			{
				return;
			}

			str3 = str1.substring(str1.lastIndexOf(" ") + 1).trim();
			str2 = paramString3 + str3;
		} else {
			if ((str1.contains(":")) && (!str1.contains("::")))
			{
				return;
			}

			str3 = str1.substring(str1.lastIndexOf(" ") + 1).trim();
			str2 = paramString3 + str3;
		}

		str1 = paramString2.trim();
		if (str1.endsWith(str2))
			str1 = str1.substring(0, str1.lastIndexOf(str2));
		else if (str1.endsWith(str3)) {
			str1 = str1.substring(0, str1.lastIndexOf(str3));
		}
		if (!ExtractType(paramString1, str1, str2, paramBoolean))
		{
			str4 = this.m_gdb.getTypeDesc(str2);
			if (str4 != null)
				ExtractType(paramString1, str4, str2, paramBoolean);
		}
	}

	private boolean ExtractType(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
	{
		String str = paramString2.trim();

		int k = 1;
		while (k == 1) {
			k = 0;
			if (str.startsWith("extern ")) {
				str = str.substring(7);
				k = 1;
			}
			if (str.startsWith("static ")) {
				str = str.substring(7);
				k = 1;
			}
			if (str.startsWith("volatile ")) {
				str = str.substring(9);
				k = 1;
			}
			if (str.startsWith("const ")) {
				str = str.substring(6);
				k = 1;
			}

		}

		if ((str.startsWith("struct ")) || (str.startsWith("union ")))
		{
			int i = str.indexOf("{");
			int j = 0;
			if (i != -1) {
				j = GetMatchingClosingBrace(str);
				if (j == -1)
				{
					System.out.println("Can not match opened brace in " + str);
					return false;
				}
				if (str.indexOf("*", j + 1) == -1)
				{
					String tmp = str.substring(i + 1, j);
					BuildVarList(paramString1, tmp, paramString3 + '.', paramBoolean);

					return true;
				}

			}
			else
			{
				return false;
			}
		}

		short s = getType(str, paramString3);
		if (s == -1) {
			return false;
		}

		for (Object localObject = getVarIterator(); ((Iterator)localObject).hasNext(); ) {
			GdbVar localGdbVar = (GdbVar)((Iterator)localObject).next();
			if (localGdbVar.get_name().compareTo(paramString3) == 0)
			{
				return true;
			}
		}

		long l = this.m_gdb.getSymbolAddress(paramString3);
		this.m_varList.add(new GdbVar(paramString3, s, paramString1, l));
		return true;
	}

	public short getType(String fileName_, String paramString2)
	{
		String fileName = fileName_.trim();

		int i = 1;
		while (i == 1) {
			i = 0;
			if (fileName.startsWith("extern ")) {
				fileName = fileName.substring(7);
				i = 1;
			}
			if (fileName.startsWith("static ")) {
				fileName = fileName.substring(7);
				i = 1;
			}
			if (fileName.startsWith("volatile ")) {
				fileName = fileName.substring(9);
				i = 1;
			}
		}
		short s;
		String str1;
		if (fileName.lastIndexOf("*") != -1)
		{
			s = 2;

			str1 = this.m_gdb.getSizeof(paramString2);
			if (str1.compareTo("1") == 0)
			{
				s = 0;
			}
			if (str1.compareTo("4") == 0)
			{
				s = 4;
			}
		}
		else if (fileName.startsWith("enum"))
		{
			s = 3;

			str1 = this.m_gdb.getSizeof(paramString2);
			if (str1.compareTo("1") == 0)
			{
				s = 1;
			}
		}
		else if (fileName.equals("unsigned char"))
		{
			s = 0;
		}
		else if (fileName.equals("char"))
		{
			s = 1;
		}
		else if (fileName.equals("signed char"))
		{
			s = 1;
		}
		else if ((fileName.equals("unsigned short")) || (fileName.equals("unsigned short int")) || (fileName.equals("short unsigned int")))
		{
			s = 2;
		}
		else if ((fileName.equals("short")) || (fileName.equals("short int")))
		{
			s = 3;
		}
		else if ((fileName.equals("signed short")) || (fileName.equals("signed short int")) || (fileName.equals("short signed int")))
		{
			s = 3;
		}
		else if (fileName.equals("unsigned int"))
		{
			s = 4;
			str1 = this.m_gdb.getSizeof(paramString2);
			if (str1.compareTo("2") == 0)
			{
				s = 2;
			}
		}
		else if (fileName.equals("int"))
		{
			s = 5;
			str1 = this.m_gdb.getSizeof(paramString2);
			if (str1.compareTo("2") == 0)
			{
				s = 3;
			}
		}
		else if (fileName.equals("signed int"))
		{
			s = 5;
			str1 = this.m_gdb.getSizeof(paramString2);
			if (str1.compareTo("2") == 0)
			{
				s = 3;
			}
		}
		else if ((fileName.equals("unsigned long")) || (fileName.equals("unsigned long int")) || (fileName.equals("long unsigned int")))
		{
			s = 4;
		}
		else if ((fileName.equals("long")) || (fileName.equals("long int")))
		{
			s = 5;
		}
		else if ((fileName.equals("signed long")) || (fileName.equals("signed long int")) || (fileName.equals("long signed int")))
		{
			s = 5;
		} else {
			if (fileName.startsWith("void"))
			{
				return -1;
			}
			if (fileName.contains("double")) {
				return 7;
			}
			if (fileName.contains("float")) {
				return 6;
			}
			return -1;
		}
		return s;
	}

	public Iterator<GdbVar> getVarIterator() {
		return this.m_varList.iterator();
	}
}

/* Location:           /home/mm/STMStudio.jar
 * Qualified Name:     com.st.stmstudio.gdb.InfoVar
 * JD-Core Version:    0.6.2
 */
