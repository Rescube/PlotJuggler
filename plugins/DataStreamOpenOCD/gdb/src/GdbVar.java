 
 public class GdbVar
 {
   private String m_name;
   private short m_type;
   private String m_filename;
   private String m_displayFilename;
   private long m_address;
 
   GdbVar(String paramString1, String paramString2)
   {
     this.m_name = paramString1;
     this.m_filename = paramString2;
     ParseFileDisplayName();
     this.m_type = -1;
     this.m_address = 0L;
   }
 
   GdbVar(String paramString1, short paramShort, String paramString2, long paramLong) {
     this.m_name = paramString1;
     this.m_filename = paramString2;
     ParseFileDisplayName();
     this.m_type = paramShort;
     this.m_address = paramLong;
   }
 
   private void ParseFileDisplayName() {
     int i = this.m_filename.lastIndexOf("\\");
     if (i != -1) {
       String str = this.m_filename.substring(0, i);
       this.m_displayFilename = (this.m_filename.substring(i + 1) + " (" + str + ")");
     } else {
       this.m_displayFilename = this.m_filename;
     }
   }
   
   public void dump()
   {
	   System.out.printf("%s %s, 0x%x\n", m_filename, m_name, m_address);
   }
 
   public String get_name() {
     return this.m_name;
   }
 
   public short get_type() {
     return this.m_type;
   }
 
   public String get_filename() {
     return this.m_displayFilename;
   }
 
   public long get_address() {
     return this.m_address;
   }
 
   public void set_address(long paramLong) {
     this.m_address = paramLong;
   }
 }

/* Location:           /home/mm/STMStudio.jar
 * Qualified Name:     com.st.stmstudio.gdb.GdbVar
 * JD-Core Version:    0.6.2
 */
