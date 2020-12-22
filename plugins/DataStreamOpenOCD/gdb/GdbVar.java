/*    */ package com.st.stmstudio.gdb;
/*    */ 
/*    */ public class GdbVar
/*    */ {
/*    */   private String m_name;
/*    */   private short m_type;
/*    */   private String m_filename;
/*    */   private String m_displayFilename;
/*    */   private long m_address;
/*    */ 
/*    */   GdbVar(String paramString1, String paramString2)
/*    */   {
/* 11 */     this.m_name = paramString1;
/* 12 */     this.m_filename = paramString2;
/* 13 */     ParseFileDisplayName();
/* 14 */     this.m_type = -1;
/* 15 */     this.m_address = 0L;
/*    */   }
/*    */ 
/*    */   GdbVar(String paramString1, short paramShort, String paramString2, long paramLong) {
/* 19 */     this.m_name = paramString1;
/* 20 */     this.m_filename = paramString2;
/* 21 */     ParseFileDisplayName();
/* 22 */     this.m_type = paramShort;
/* 23 */     this.m_address = paramLong;
/*    */   }
/*    */ 
/*    */   private void ParseFileDisplayName() {
/* 27 */     int i = this.m_filename.lastIndexOf("\\");
/* 28 */     if (i != -1) {
/* 29 */       String str = this.m_filename.substring(0, i);
/* 30 */       this.m_displayFilename = (this.m_filename.substring(i + 1) + " (" + str + ")");
/*    */     } else {
/* 32 */       this.m_displayFilename = this.m_filename;
/*    */     }
/*    */   }
/*    */ 
/*    */   public String get_name() {
/* 37 */     return this.m_name;
/*    */   }
/*    */ 
/*    */   public short get_type() {
/* 41 */     return this.m_type;
/*    */   }
/*    */ 
/*    */   public String get_filename() {
/* 45 */     return this.m_displayFilename;
/*    */   }
/*    */ 
/*    */   public long get_address() {
/* 49 */     return this.m_address;
/*    */   }
/*    */ 
/*    */   public void set_address(long paramLong) {
/* 53 */     this.m_address = paramLong;
/*    */   }
/*    */ }

/* Location:           /home/mm/STMStudio.jar
 * Qualified Name:     com.st.stmstudio.gdb.GdbVar
 * JD-Core Version:    0.6.2
 */