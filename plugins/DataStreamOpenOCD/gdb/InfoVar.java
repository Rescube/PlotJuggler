/*     */ package com.st.stmstudio.gdb;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class InfoVar
/*     */ {
/*     */   private String m_infoVarStr;
/*     */   private List<GdbVar> m_varList;
/*     */   private GdbExec m_gdb;
/*  15 */   private String FILE_MARKER_START = "File ";
/*  16 */   private String FILE_MARKER_END = ":\r\n";
/*  17 */   private String NON_DEBUG_MARKER = "Non-debugging symbols:";
/*     */ 
/*     */   InfoVar(GdbExec paramGdbExec) {
/*  20 */     this.m_gdb = paramGdbExec;
/*     */   }
/*     */ 
/*     */   InfoVar(GdbExec paramGdbExec, String paramString) {
/*  24 */     this.m_infoVarStr = paramString;
/*  25 */     this.m_varList = new ArrayList();
/*  26 */     this.m_gdb = paramGdbExec;
/*     */   }
/*     */ 
/*     */   public void Parse(boolean paramBoolean)
/*     */   {
/*  43 */     this.m_varList.clear();
/*     */ 
/*  45 */     if (this.m_infoVarStr.isEmpty()) {
/*  46 */       return;
/*     */     }
/*     */ 
/*  50 */     int i = this.m_infoVarStr.indexOf(this.FILE_MARKER_START);
/*     */ 
/*  52 */     int k = 0;
/*     */ 
/*  55 */     while ((i != -1) && (k == 0))
/*     */     {
/*  57 */       int j = this.m_infoVarStr.indexOf(this.FILE_MARKER_START, i + 1);
/*  58 */       if (j == -1)
/*     */       {
/*  60 */         j = this.m_infoVarStr.indexOf(this.NON_DEBUG_MARKER, i + 1);
/*  61 */         k = 1;
/*     */       }
/*  63 */       if (j == -1)
/*     */       {
/*  65 */         j = this.m_infoVarStr.length() - 1;
/*  66 */         k = 1;
/*     */       }
/*     */ 
/*  70 */       int m = this.m_infoVarStr.indexOf(this.FILE_MARKER_END, i);
/*  71 */       if (m == -1) {
/*  72 */         System.out.println("Info var parsing error while getting filename");
/*  73 */         return;
/*     */       }
/*  75 */       String str = this.m_infoVarStr.substring(i + this.FILE_MARKER_START.length(), m);
/*     */ 
/*  78 */       BuildVarList(str, this.m_infoVarStr.substring(m + this.FILE_MARKER_END.length(), j), "", paramBoolean);
/*     */ 
/*  83 */       i = j;
/*     */     }
/*     */   }
/*     */ 
/*     */   private int GetMatchingClosingBrace(String paramString)
/*     */   {
/*  91 */     int i = paramString.indexOf("{");
/*  92 */     if (i == -1) {
/*  93 */       return 0;
/*     */     }
/*     */ 
/*  97 */     int j = 1;
/*  98 */     int k = paramString.indexOf("{", i + 1);
/*  99 */     int m = paramString.indexOf("}", i + 1);
/* 100 */     while (j != 0) {
/* 101 */       if (m == -1)
/*     */       {
/* 103 */         return -1;
/*     */       }
/* 105 */       if (k != -1)
/*     */       {
/* 107 */         if (k < m)
/*     */         {
/* 109 */           j++;
/* 110 */           k = paramString.indexOf("{", k + 1);
/*     */         }
/*     */       }
/*     */ 
/* 114 */       j--;
/* 115 */       if (j > 0)
/*     */       {
/* 117 */         m = paramString.indexOf("}", m + 1);
/*     */       }
/*     */     }
/* 120 */     return m;
/*     */   }
/*     */ 
/*     */   private void BuildVarList(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
/*     */   {
/* 129 */     int i = 0;
/*     */ 
/* 136 */     while (i < paramString2.length()) {
/* 137 */       String str2 = paramString2.substring(i);
/* 138 */       int j = str2.indexOf(";");
/* 139 */       int k = str2.indexOf("{");
/* 140 */       int m = 0;
/* 141 */       if ((k != -1) && (k < j))
/*     */       {
/* 143 */         m = GetMatchingClosingBrace(str2);
/* 144 */         if (m == -1)
/*     */         {
/* 146 */           System.out.println("Info var parsing error: opened brace not closed");
/* 147 */           return;
/*     */         }
/*     */       }
/*     */ 
/* 151 */       int n = str2.indexOf(";", m);
/* 152 */       if (n == -1)
/*     */         return;
/*     */       String str1;
/* 157 */       if (m != 0) {
/* 158 */         str1 = str2.substring(0, k) + str2.substring(m + 1, n);
/*     */       }
/*     */       else {
/* 161 */         str1 = str2.substring(0, n);
/*     */       }
/* 163 */       ExtractIdentifier(paramString1, str1, paramString3, paramBoolean);
/* 164 */       i += n + 1;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void ExtractIdentifier(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
/*     */   {
/* 177 */     String str1 = paramString2.trim();
/*     */ 
/* 180 */     int j = str1.indexOf("[");
/*     */     String str2;
/*     */     String str4;
/* 181 */     if (j != -1)
/*     */     {
/* 183 */       if (j != str1.lastIndexOf("["))
/*     */       {
/* 185 */         return;
/*     */       }
/* 187 */       String str5 = str1.substring(str1.lastIndexOf(" ", j) + 1, j).trim();
/*     */ 
/* 189 */       if (str5.lastIndexOf("*") != -1)
/* 190 */         str5 = str5.substring(str5.lastIndexOf("*") + 1).trim();
/*     */       int i;
/* 192 */       if (paramBoolean == true)
/*     */       {
/* 194 */         String str6 = str1.substring(j + 1, str1.indexOf("]", j));
/* 195 */         i = Integer.parseInt(str6);
/*     */       }
/*     */       else
/*     */       {
/* 199 */         i = 1;
/*     */       }
/*     */ 
/* 202 */       str2 = paramString3 + str5 + "[0]";
/* 203 */       str4 = this.m_gdb.getTypeDesc(str2);
/* 204 */       for (int m = 0; m < i; m++)
/*     */       {
/* 206 */         str2 = paramString3 + str5 + "[" + m + "]";
/* 207 */         if (str4 != null)
/* 208 */           ExtractType(paramString1, str4, str2, paramBoolean);
/*     */       }
/*     */       return;
/*     */     }
/*     */     String str3;
/* 215 */     if (str1.lastIndexOf("*") != -1)
/*     */     {
/* 217 */       int k = str1.indexOf(")");
/* 218 */       if (k != -1)
/*     */       {
/* 220 */         str1 = str1.substring(0, k);
/*     */       }
/*     */ 
/* 223 */       str1 = str1.substring(str1.lastIndexOf("*") + 1).trim();
/*     */ 
/* 225 */       if (str1.lastIndexOf(" ") != -1) {
/* 226 */         str1 = str1.substring(str1.lastIndexOf(" ") + 1).trim();
/*     */       }
/* 228 */       if (str1.isEmpty())
/*     */       {
/* 230 */         return;
/*     */       }
/* 232 */       str2 = paramString3 + str1;
/* 233 */       str3 = str1;
/*     */     }
/* 235 */     else if (str1.contains("::"))
/*     */     {
/* 237 */       if (str1.contains("<"))
/*     */       {
/* 239 */         return;
/*     */       }
/* 241 */       if ((str1.startsWith("const ")) || (str1.startsWith("static const ")))
/*     */       {
/* 243 */         return;
/*     */       }
/*     */ 
/* 246 */       str3 = str1.substring(str1.lastIndexOf(" ") + 1).trim();
/* 247 */       str2 = paramString3 + str3;
/*     */     } else {
/* 249 */       if ((str1.contains(":")) && (!str1.contains("::")))
/*     */       {
/* 251 */         return;
/*     */       }
/*     */ 
/* 255 */       str3 = str1.substring(str1.lastIndexOf(" ") + 1).trim();
/* 256 */       str2 = paramString3 + str3;
/*     */     }
/*     */ 
/* 262 */     str1 = paramString2.trim();
/* 263 */     if (str1.endsWith(str2))
/* 264 */       str1 = str1.substring(0, str1.lastIndexOf(str2));
/* 265 */     else if (str1.endsWith(str3)) {
/* 266 */       str1 = str1.substring(0, str1.lastIndexOf(str3));
/*     */     }
/* 268 */     if (!ExtractType(paramString1, str1, str2, paramBoolean))
/*     */     {
/* 270 */       str4 = this.m_gdb.getTypeDesc(str2);
/* 271 */       if (str4 != null)
/* 272 */         ExtractType(paramString1, str4, str2, paramBoolean);
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean ExtractType(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
/*     */   {
/* 289 */     String str = paramString2.trim();
/*     */ 
/* 292 */     int k = 1;
/* 293 */     while (k == 1) {
/* 294 */       k = 0;
/* 295 */       if (str.startsWith("extern ")) {
/* 296 */         str = str.substring(7);
/* 297 */         k = 1;
/*     */       }
/* 299 */       if (str.startsWith("static ")) {
/* 300 */         str = str.substring(7);
/* 301 */         k = 1;
/*     */       }
/* 303 */       if (str.startsWith("volatile ")) {
/* 304 */         str = str.substring(9);
/* 305 */         k = 1;
/*     */       }
/* 307 */       if (str.startsWith("const ")) {
/* 308 */         str = str.substring(6);
/* 309 */         k = 1;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 314 */     if ((str.startsWith("struct ")) || (str.startsWith("union ")))
/*     */     {
/* 316 */       int i = str.indexOf("{");
/* 317 */       int j = 0;
/* 318 */       if (i != -1) {
/* 319 */         j = GetMatchingClosingBrace(str);
/* 320 */         if (j == -1)
/*     */         {
/* 322 */           System.out.println("Can not match opened brace in " + str);
/* 323 */           return false;
/*     */         }
/* 325 */         if (str.indexOf("*", j + 1) == -1)
/*     */         {
/* 332 */           localObject = str.substring(i + 1, j);
/* 333 */           BuildVarList(paramString1, (String)localObject, paramString3 + '.', paramBoolean);
/*     */ 
/* 336 */           return true;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 342 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 346 */     short s = getType(str, paramString3);
/* 347 */     if (s == -1) {
/* 348 */       return false;
/*     */     }
/*     */ 
/* 463 */     for (Object localObject = getVarIterator(); ((Iterator)localObject).hasNext(); ) {
/* 464 */       GdbVar localGdbVar = (GdbVar)((Iterator)localObject).next();
/* 465 */       if (localGdbVar.get_name().compareTo(paramString3) == 0)
/*     */       {
/* 467 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 471 */     long l = this.m_gdb.getSymbolAddress(paramString3);
/* 472 */     this.m_varList.add(new GdbVar(paramString3, s, paramString1, l));
/* 473 */     return true;
/*     */   }
/*     */ 
/*     */   public short getType(String paramString1, String paramString2)
/*     */   {
/* 480 */     String str2 = paramString1.trim();
/*     */ 
/* 483 */     int i = 1;
/* 484 */     while (i == 1) {
/* 485 */       i = 0;
/* 486 */       if (str2.startsWith("extern ")) {
/* 487 */         str2 = str2.substring(7);
/* 488 */         i = 1;
/*     */       }
/* 490 */       if (str2.startsWith("static ")) {
/* 491 */         str2 = str2.substring(7);
/* 492 */         i = 1;
/*     */       }
/* 494 */       if (str2.startsWith("volatile ")) {
/* 495 */         str2 = str2.substring(9);
/* 496 */         i = 1;
/*     */       }
/*     */     }
/*     */     short s;
/*     */     String str1;
/* 500 */     if (str2.lastIndexOf("*") != -1)
/*     */     {
/* 503 */       s = 2;
/*     */ 
/* 505 */       str1 = this.m_gdb.getSizeof(paramString2);
/* 506 */       if (str1.compareTo("1") == 0)
/*     */       {
/* 508 */         s = 0;
/*     */       }
/* 510 */       if (str1.compareTo("4") == 0)
/*     */       {
/* 512 */         s = 4;
/*     */       }
/*     */     }
/* 515 */     else if (str2.startsWith("enum"))
/*     */     {
/* 518 */       s = 3;
/*     */ 
/* 520 */       str1 = this.m_gdb.getSizeof(paramString2);
/* 521 */       if (str1.compareTo("1") == 0)
/*     */       {
/* 523 */         s = 1;
/*     */       }
/*     */     }
/* 526 */     else if (str2.equals("unsigned char"))
/*     */     {
/* 528 */       s = 0;
/*     */     }
/* 530 */     else if (str2.equals("char"))
/*     */     {
/* 532 */       s = 1;
/*     */     }
/* 534 */     else if (str2.equals("signed char"))
/*     */     {
/* 536 */       s = 1;
/*     */     }
/* 538 */     else if ((str2.equals("unsigned short")) || (str2.equals("unsigned short int")) || (str2.equals("short unsigned int")))
/*     */     {
/* 540 */       s = 2;
/*     */     }
/* 542 */     else if ((str2.equals("short")) || (str2.equals("short int")))
/*     */     {
/* 544 */       s = 3;
/*     */     }
/* 546 */     else if ((str2.equals("signed short")) || (str2.equals("signed short int")) || (str2.equals("short signed int")))
/*     */     {
/* 548 */       s = 3;
/*     */     }
/* 550 */     else if (str2.equals("unsigned int"))
/*     */     {
/* 553 */       s = 4;
/* 554 */       str1 = this.m_gdb.getSizeof(paramString2);
/* 555 */       if (str1.compareTo("2") == 0)
/*     */       {
/* 557 */         s = 2;
/*     */       }
/*     */     }
/* 560 */     else if (str2.equals("int"))
/*     */     {
/* 563 */       s = 5;
/* 564 */       str1 = this.m_gdb.getSizeof(paramString2);
/* 565 */       if (str1.compareTo("2") == 0)
/*     */       {
/* 567 */         s = 3;
/*     */       }
/*     */     }
/* 570 */     else if (str2.equals("signed int"))
/*     */     {
/* 573 */       s = 5;
/* 574 */       str1 = this.m_gdb.getSizeof(paramString2);
/* 575 */       if (str1.compareTo("2") == 0)
/*     */       {
/* 577 */         s = 3;
/*     */       }
/*     */     }
/* 580 */     else if ((str2.equals("unsigned long")) || (str2.equals("unsigned long int")) || (str2.equals("long unsigned int")))
/*     */     {
/* 582 */       s = 4;
/*     */     }
/* 584 */     else if ((str2.equals("long")) || (str2.equals("long int")))
/*     */     {
/* 586 */       s = 5;
/*     */     }
/* 588 */     else if ((str2.equals("signed long")) || (str2.equals("signed long int")) || (str2.equals("long signed int")))
/*     */     {
/* 590 */       s = 5;
/*     */     } else {
/* 592 */       if (str2.startsWith("void"))
/*     */       {
/* 594 */         return -1;
/*     */       }
/* 596 */       if (str2.contains("double")) {
/* 597 */         return 7;
/*     */       }
/* 599 */       if (str2.contains("float")) {
/* 600 */         return 6;
/*     */       }
/*     */ 
/* 604 */       System.out.println(str2);
/* 605 */       return -1;
/*     */     }
/* 607 */     return s;
/*     */   }
/*     */ 
/*     */   public Iterator<GdbVar> getVarIterator() {
/* 611 */     return this.m_varList.iterator();
/*     */   }
/*     */ }

/* Location:           /home/mm/STMStudio.jar
 * Qualified Name:     com.st.stmstudio.gdb.InfoVar
 * JD-Core Version:    0.6.2
 */