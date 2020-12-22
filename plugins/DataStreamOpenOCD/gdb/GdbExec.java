/*     */ package com.st.stmstudio.gdb;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Iterator;
/*     */ import javax.swing.JOptionPane;
/*     */ 
/*     */ public class GdbExec
/*     */ {
/*  12 */   OutputStream m_stdin = null;
/*  13 */   InputStream m_stderr = null;
/*  14 */   BufferedInputStream m_stdout = null;
/*     */   Process m_p;
/*  16 */   boolean m_bIsRunning = false;
/*  17 */   boolean verbose = true;
/*     */ 
/*  20 */   final int IO_ERROR = 0;
/*  21 */   final int CMD_ERROR = 1;
/*  22 */   final int CMD_IN_PROGRESS = 2;
/*  23 */   final int CMD_END_OK = 3;
/*     */ 
/*  25 */   final String GDB_PROMPT = "(gdb) ";
/*  26 */   final int GDB_PROMPT_SIZE = 6;
/*     */ 
/*  28 */   int m_status = 3;
/*  29 */   String m_stdoutStr = "";
/*  30 */   String m_stderrStr = "";
/*  31 */   long addressOffset = 0L;
/*     */   InfoVar m_getGdbVar;
/*     */ 
/*     */   public GdbExec(long paramLong)
/*     */   {
/*  41 */     this.addressOffset = paramLong;
/*     */   }
/*     */ 
/*     */   public GdbExec(String paramString, long paramLong) {
/*  45 */     int i = 3;
/*  46 */     if (!this.m_bIsRunning) {
/*  47 */       runGdb();
/*  48 */       if (this.m_bIsRunning == true)
/*     */       {
/*  50 */         i = sendCmdAndWaitAnswer("");
/*     */ 
/*  52 */         if (i == 3) {
/*  53 */           i = sendCmdAndWaitAnswer("symbol-file \"" + paramString + "\"\n");
/*     */         }
/*  55 */         this.addressOffset = paramLong;
/*  56 */         this.verbose = false;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isRunning() {
/*  62 */     return this.m_bIsRunning;
/*     */   }
/*     */ 
/*     */   public void runGdb() {
/*     */     try {
/*  67 */       this.m_p = Runtime.getRuntime().exec("dll/gdb.exe -stmstudio");
/*  68 */       this.m_bIsRunning = true;
/*     */     } catch (IOException localIOException) {
/*  70 */       JOptionPane.showMessageDialog(null, "Failed launching gdb.exe: " + localIOException.getMessage(), "Message", 0);
/*     */     }
/*     */ 
/*  74 */     this.m_stdin = this.m_p.getOutputStream();
/*  75 */     this.m_stderr = this.m_p.getErrorStream();
/*  76 */     this.m_stdout = new BufferedInputStream(this.m_p.getInputStream());
/*     */   }
/*     */ 
/*     */   public String readAnswer(InputStream paramInputStream, boolean paramBoolean)
/*     */   {
/*  88 */     String str = new String("");
/*     */     try
/*     */     {
/*     */       int i;
/*  92 */       if (paramBoolean == true)
/*     */       {
/*     */         do
/*     */         {
/*  97 */           i = paramInputStream.available();
/*  98 */           if (i == 0) {
/*     */             try
/*     */             {
/* 101 */               Thread.sleep(1L);
/*     */             } catch (InterruptedException localInterruptedException) {
/* 103 */               JOptionPane.showMessageDialog(null, "Thread wakes up by external: " + localInterruptedException.getMessage(), "Message", 0);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 108 */         while (i == 0);
/*     */       }
/*     */       else
/*     */       {
/* 118 */         i = paramInputStream.available();
/*     */       }
/* 120 */       while (i > 0) {
/* 121 */         byte[] arrayOfByte = new byte[i];
/* 122 */         paramInputStream.read(arrayOfByte, 0, i);
/* 123 */         str = str + new String(arrayOfByte);
/*     */ 
/* 125 */         i = paramInputStream.available();
/*     */       }
/*     */     } catch (IOException localIOException) {
/* 128 */       this.m_status = 0;
/* 129 */       JOptionPane.showMessageDialog(null, "Failed getting gdb answer: " + localIOException.getMessage(), "Message", 0);
/*     */     }
/*     */ 
/* 133 */     return str;
/*     */   }
/*     */ 
/*     */   private void sendCmd(String paramString)
/*     */   {
/*     */     try
/*     */     {
/* 141 */       this.m_stdin.write(paramString.getBytes());
/* 142 */       this.m_stdin.flush();
/*     */     } catch (IOException localIOException) {
/* 144 */       this.m_status = 0;
/* 145 */       JOptionPane.showMessageDialog(null, "Failed sending cmd " + paramString + ": " + localIOException.getMessage(), "Message", 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/*     */     try
/*     */     {
/* 153 */       this.m_stdin.close();
/* 154 */       this.m_stderr.close();
/* 155 */       this.m_stdout.close();
/* 156 */       this.m_p.destroy();
/* 157 */       this.m_bIsRunning = false;
/*     */     } catch (IOException localIOException) {
/* 159 */       JOptionPane.showMessageDialog(null, "Failed closing stream: " + localIOException.getMessage(), "Message", 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int sendCmdAndWaitAnswer(String paramString)
/*     */   {
/* 175 */     if (!paramString.isEmpty())
/*     */     {
/* 178 */       sendCmd(paramString);
/* 179 */       if (this.m_status == 0) {
/* 180 */         return 0;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 186 */     this.m_status = 2;
/* 187 */     this.m_stdoutStr = "";
/*     */ 
/* 190 */     while (this.m_status == 2) {
/* 191 */       this.m_stdoutStr += readAnswer(this.m_stdout, true);
/* 192 */       if (this.m_status == 0) {
/* 193 */         return 0;
/*     */       }
/* 195 */       if (this.m_stdoutStr.endsWith("(gdb) "))
/*     */       {
/* 198 */         this.m_status = 3;
/*     */       }
/*     */       else
/*     */       {
/* 203 */         this.m_status = 2;
/*     */       }
/*     */ 
/* 206 */       this.m_stderrStr = readAnswer(this.m_stderr, false);
/* 207 */       if (this.m_status == 0) {
/* 208 */         return 0;
/*     */       }
/* 210 */       if ((!this.m_stderrStr.isEmpty()) && (this.verbose)) {
/* 211 */         JOptionPane.showMessageDialog(null, "GDB cmd [" + paramString.trim() + "] failed: " + this.m_stderrStr, "Message", 0);
/*     */ 
/* 214 */         this.m_status = 1;
/*     */       }
/*     */     }
/* 217 */     return this.m_status;
/*     */   }
/*     */ 
/*     */   public Iterator<GdbVar> getVarIterator() {
/* 221 */     return this.m_getGdbVar.getVarIterator();
/*     */   }
/*     */ 
/*     */   public String getTypeDesc(String paramString) {
/* 225 */     int i = 3;
/* 226 */     String str = null;
/*     */ 
/* 228 */     if (this.m_bIsRunning == true) {
/* 229 */       i = sendCmdAndWaitAnswer("ptype " + paramString + "\n");
/* 230 */       if (i == 3)
/*     */       {
/* 232 */         if ((this.m_stdoutStr.startsWith("type = ")) && (this.m_stdoutStr.endsWith("(gdb) "))) {
/* 233 */           str = new String(this.m_stdoutStr.substring(7, this.m_stdoutStr.length() - 6).trim());
/*     */         }
/*     */       }
/*     */     }
/* 237 */     return str;
/*     */   }
/*     */ 
/*     */   public short getType(String paramString) {
/* 241 */     InfoVar localInfoVar = new InfoVar(this);
/* 242 */     return localInfoVar.getType(getTypeDesc(paramString), paramString);
/*     */   }
/*     */ 
/*     */   public String getSizeof(String paramString) {
/* 246 */     int i = 3;
/* 247 */     String str = "";
/*     */ 
/* 249 */     if (this.m_bIsRunning == true) {
/* 250 */       i = sendCmdAndWaitAnswer("p sizeof " + paramString + "\n");
/* 251 */       if (i == 3)
/*     */       {
/* 253 */         if ((this.m_stdoutStr.indexOf("=") != -1) && (this.m_stdoutStr.endsWith("(gdb) "))) {
/* 254 */           str = new String(this.m_stdoutStr.substring(this.m_stdoutStr.indexOf("=") + 1, this.m_stdoutStr.length() - 6).trim());
/*     */         }
/*     */       }
/*     */     }
/* 258 */     return str;
/*     */   }
/*     */ 
/*     */   public boolean buildVarList(String paramString, boolean paramBoolean) {
/* 262 */     int i = 3;
/* 263 */     boolean bool = false;
/*     */ 
/* 265 */     if (!this.m_bIsRunning) {
/* 266 */       runGdb();
/* 267 */       if (this.m_bIsRunning == true)
/*     */       {
/* 269 */         i = sendCmdAndWaitAnswer("");
/*     */       }
/*     */     }
/* 272 */     if (this.m_bIsRunning == true) {
/* 273 */       if (i == 3) {
/* 274 */         i = sendCmdAndWaitAnswer("symbol-file \"" + paramString + "\"\n");
/*     */       }
/* 276 */       if (i == 3) {
/* 277 */         i = sendCmdAndWaitAnswer("info variables\n");
/* 278 */         if (i == 3) {
/* 279 */           bool = true;
/*     */ 
/* 281 */           this.m_getGdbVar = new InfoVar(this, this.m_stdoutStr);
/*     */ 
/* 283 */           this.m_getGdbVar.Parse(paramBoolean);
/*     */         }
/*     */       }
/* 286 */       stop();
/*     */     }
/* 288 */     return bool;
/*     */   }
/*     */ 
/*     */   public long getSymbolAddress(String paramString) {
/* 292 */     int i = 3;
/* 293 */     long l = -1L;
/*     */ 
/* 297 */     i = sendCmdAndWaitAnswer("print /x &(" + paramString + ")\n");
/* 298 */     if (i == 3)
/*     */     {
/* 300 */       int j = 0;
/*     */ 
/* 302 */       String str = "= ";
/* 303 */       int k = this.m_stdoutStr.indexOf(str);
/* 304 */       if (k != -1) {
/* 305 */         int m = k + str.length();
/*     */ 
/* 307 */         int n = this.m_stdoutStr.indexOf("\r", m);
/* 308 */         if (n != -1) {
/* 309 */           l = this.addressOffset + Long.decode(this.m_stdoutStr.substring(m, n)).longValue();
/* 310 */           j = 1;
/*     */         }
/*     */       }
/* 313 */       if ((j == 0) && (this.verbose)) {
/* 314 */         JOptionPane.showMessageDialog(null, "Error while extracting address from string: " + this.m_stdoutStr + ": set to 0", "Message", 0);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 320 */     return l;
/*     */   }
/*     */ }

/* Location:           /home/mm/STMStudio.jar
 * Qualified Name:     com.st.stmstudio.gdb.GdbExec
 * JD-Core Version:    0.6.2
 */