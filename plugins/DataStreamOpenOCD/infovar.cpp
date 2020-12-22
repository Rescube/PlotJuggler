#include "infovar.h"

#include "gdbvar.h"

#include <QDebug>

InfoVar::InfoVar(GdbExec *paramGdbExec) :
  m_gdb(paramGdbExec)
{

}

void InfoVar::Parse(const QString &m_infoVarStr, bool parambool)
{
  qDeleteAll(m_varList);
  m_varList.clear();
  if (m_infoVarStr.isEmpty()) {
    return;
  }

  int i = m_infoVarStr.indexOf(FILE_MARKER_START);

  int k = 0;

  while ((i != -1) && (k == 0))
  {
    int j = m_infoVarStr.indexOf(FILE_MARKER_START, i + 1);
    if (j == -1)
    {
      j = m_infoVarStr.indexOf(NON_DEBUG_MARKER, i + 1);
      k = 1;
    }
    if (j == -1)
    {
      j = m_infoVarStr.length() - 1;
      k = 1;
    }

    int m = m_infoVarStr.indexOf(FILE_MARKER_END, i);
    if (m == -1) {
      qWarning() << "Info var parsing error while getting filename";
      return;
    }
    QString str = m_infoVarStr.mid(i + FILE_MARKER_START.length(), m);

    BuildVarList(str, m_infoVarStr.mid(m + FILE_MARKER_END.length(), j), "", parambool);

    i = j;
  }
}

int InfoVar::GetMatchingClosingBrace(const QString &paramString)
{
  int i = paramString.indexOf("{");
  if (i == -1)
    return 0;

  int j = 1;
  int k = paramString.indexOf("{", i + 1);
  int ret = paramString.indexOf("}", i + 1);
  while (j != 0) {
    if (ret == -1)
      return -1;

    if (k != -1) {
      if (k < ret) {
        j++;
        k = paramString.indexOf("{", k + 1);
      }
    }

    j--;
    if (j > 0)
      ret = paramString.indexOf("}", ret + 1);
  }
  return ret;
}

void InfoVar::BuildVarList(const QString &paramString1, const QString &paramString2, const QString &paramString3, bool parambool)
{
  int i = 0;
  while (i < paramString2.length()) {
    QString str2 = paramString2.mid(i);
    int j = str2.indexOf(";");
    int k = str2.indexOf("{");
    int m = 0;
    if ((k != -1) && (k < j))
    {
      m = GetMatchingClosingBrace(str2);
      if (m == -1)
      {
        qWarning() << "Info var parsing error: opened brace not closed";
        return;
      }
    }

    int n = str2.indexOf(";", m);
    if (n == -1)
      return;
    QString str1;
    if (m != 0) {
      str1 = str2.mid(0, k) + str2.mid(m + 1, n);
    } else {
      str1 = str2.mid(0, n);
    }
    ExtractIdentifier(paramString1, str1, paramString3, parambool);
    i += n + 1;
  }
}

void InfoVar::ExtractIdentifier(const QString &paramString1, const QString &paramString2, const QString &paramString3, bool parambool)
{
  QString str1 = paramString2.trimmed();

  int j = str1.indexOf("[");
  QString str2;
  QString str4;
  if (j != -1)
  {
    if (j != str1.lastIndexOf("["))
    {
      return;
    }
    QString str5 = str1.mid(str1.lastIndexOf(" ", j) + 1, j).trimmed();

    if (str5.lastIndexOf("*") != -1)
      str5 = str5.mid(str5.lastIndexOf("*") + 1).trimmed();
    int i;
    if (parambool == true)
    {
      QString str6 = str1.mid(j + 1, str1.indexOf("]", j));
      i = str6.toInt();
    }
    else
    {
      i = 1;
    }

    str2 = paramString3 + str5 + "[0]";
    str4 = m_gdb->getTypeDesc(str2);
    for (int m = 0; m < i; m++)
    {
      str2 = paramString3 + str5 + "[" + m + "]";
      if (!str4.isEmpty())
        ExtractType(paramString1, str4, str2, parambool);
    }
    return;
  }
  QString str3;
  if (str1.lastIndexOf("*") != -1)
  {
    int k = str1.indexOf(")");
    if (k != -1)
    {
      str1 = str1.mid(0, k);
    }

    str1 = str1.mid(str1.lastIndexOf("*") + 1).trimmed();

    if (str1.lastIndexOf(" ") != -1) {
      str1 = str1.mid(str1.lastIndexOf(" ") + 1).trimmed();
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

    str3 = str1.mid(str1.lastIndexOf(" ") + 1).trimmed();
    str2 = paramString3 + str3;
  } else {
    if ((str1.contains(":")) && (!str1.contains("::")))
    {
      return;
    }

    str3 = str1.mid(str1.lastIndexOf(" ") + 1).trimmed();
    str2 = paramString3 + str3;
  }

  str1 = paramString2.trimmed();
  if (str1.endsWith(str2))
    str1 = str1.mid(0, str1.lastIndexOf(str2));
  else if (str1.endsWith(str3)) {
    str1 = str1.mid(0, str1.lastIndexOf(str3));
  }
  if (!ExtractType(paramString1, str1, str2, parambool))
  {
    str4 = m_gdb->getTypeDesc(str2);
    if (!str4.isEmpty())
      ExtractType(paramString1, str4, str2, parambool);
  }
}

bool InfoVar::ExtractType(const QString &paramString1, const QString &paramString2, const QString &name, bool parambool)
{
  QString str = paramString2.trimmed();

  int k = 1;
  while (k == 1) {
    k = 0;
    if (str.startsWith("extern ")) {
      str = str.mid(7);
      k = 1;
    }
    if (str.startsWith("static ")) {
      str = str.mid(7);
      k = 1;
    }
    if (str.startsWith("volatile ")) {
      str = str.mid(9);
      k = 1;
    }
    if (str.startsWith("const ")) {
      str = str.mid(6);
      k = 1;
    }

  }

  if ((str.startsWith("struct ")) || (str.startsWith("union "))) {
    int i = str.indexOf("{");
    int j = 0;
    if (i != -1) {
      j = GetMatchingClosingBrace(str);
      if (j == -1) {
        qWarning() << "Can not match opened brace in " + str;
        return false;
      }

      if (str.indexOf("*", j + 1) == -1) {
        BuildVarList(paramString1, str.mid(i + 1, j), name + '.', parambool);
        return true;
      }

    } else {
      return false;
    }
  }

  short s = getType(str, name);
  if (s == -1) {
    return false;
  }

  for (const auto *localGdbVar : m_varList) {
    if (localGdbVar->get_name() == name) {
      return true;
    }
  }

  long l = m_gdb->getSymbolAddress(name);
  m_varList.append(new GdbVar(name, s, paramString1, l));
  return true;
}

// FIXME this should return with an enum!
short InfoVar::getType(const QString &paramString1, const QString &paramString2)
{
  QString str2 = paramString1.trimmed();
  int i = 1;
  while (i == 1) {
    i = 0;
    if (str2.startsWith("extern ")) {
      str2 = str2.mid(7);
      i = 1;
    }
    if (str2.startsWith("static ")) {
      str2 = str2.mid(7);
      i = 1;
    }
    if (str2.startsWith("volatile ")) {
      str2 = str2.mid(9);
      i = 1;
    }
  }
  short ret;
  QString str1;
  if (str2.lastIndexOf("*") != -1) {
    ret = 2;
    str1 = m_gdb->getSizeof(paramString2);
    if (str1 == "1") {
      ret = 0;
    } else if (str1 == "4") {
      ret = 4;
    }
  }
  else if (str2.startsWith("enum"))
  {
    ret = 3;
    str1 = m_gdb->getSizeof(paramString2);
    if (str1 == "1") {
      ret = 1;
    }
  }
  else if (str2 == "unsigned char")
  {
    ret = 0;
  }
  else if (str2 == "char")
  {
    ret = 1;
  }
  else if (str2 == "signed char")
  {
    ret = 1;
  }
  else if ((str2 == "unsigned short") || (str2 == "unsigned short int") || (str2 == "short unsigned int"))
  {
    ret = 2;
  }
  else if ((str2 == "short") || (str2 == "short int"))
  {
    ret = 3;
  }
  else if ((str2 == "signed short") || (str2 == "signed short int") || (str2 == "short signed int"))
  {
    ret = 3;
  }
  else if (str2 == "unsigned int")
  {
    ret = 4;
    str1 = m_gdb->getSizeof(paramString2);
    if (str1 == "2")
    {
      ret = 2;
    }
  }
  else if (str2 == "int")
  {
    ret = 5;
    str1 = m_gdb->getSizeof(paramString2);
    if (str1 == "2")
    {
      ret = 3;
    }
  }
  else if (str2 == "signed int")
  {
    ret = 5;
    str1 = m_gdb->getSizeof(paramString2);
    if (str1 == "2")
    {
      ret = 3;
    }
  }
  else if ((str2 == "unsigned long") || (str2 == "unsigned long int") || (str2 == "long unsigned int"))
  {
    ret = 4;
  }
  else if ((str2 == "long") || (str2 == "long int"))
  {
    ret = 5;
  }
  else if (str2 == "signed long" || str2 == "signed long int" || str2 == "long signed int")
  {
    ret = 5;
  } else {
    if (str2.startsWith("void"))
    {
      return -1;
    }
    if (str2.contains("double")) {
      return 7;
    }
    if (str2.contains("float")) {
      return 6;
    }

    qWarning() << str2;
    return -1;
  }
  return ret;
}
