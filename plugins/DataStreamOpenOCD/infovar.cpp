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

  int fileMarker = m_infoVarStr.indexOf(FILE_MARKER_START);
  bool finished = false;
  while ((fileMarker != -1) && !finished) {
    int nextFileMarker = m_infoVarStr.indexOf(FILE_MARKER_START, fileMarker + 1);
    if (nextFileMarker == -1) {
      nextFileMarker = m_infoVarStr.indexOf(NON_DEBUG_MARKER, fileMarker + 1);
      finished = true;
    }

    if (nextFileMarker == -1) {
      // if no "Non-debugging symbols:" found at the end of the output
      // put the nextFileMarker index to the end of the output
      // BuildVarList will use this index
      nextFileMarker = m_infoVarStr.length() - 1;
      finished = true;
    }

    int fileNameEndPosition = m_infoVarStr.indexOf(FILE_MARKER_END, fileMarker);
    if (fileNameEndPosition == -1) {
      qWarning() << "Info var parsing error while getting filename";
      return;
    }
    const auto fileName = m_infoVarStr.mid(fileMarker + FILE_MARKER_START.length(), fileNameEndPosition - (fileMarker + FILE_MARKER_START.length()));
    BuildVarList(fileName, m_infoVarStr.mid(fileNameEndPosition + FILE_MARKER_END.length(), nextFileMarker - (fileNameEndPosition + FILE_MARKER_END.length())), "", parambool);
    fileMarker = nextFileMarker;
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

void InfoVar::BuildVarList(const QString &fileName, const QString &paramString2, const QString &paramString3, bool parambool)
{
  qWarning() << "BuildVarList" << fileName << paramString2 << paramString3;
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
    ExtractIdentifier(fileName, str1, paramString3, parambool);
    i += n + 1;
  }
}

void InfoVar::ExtractIdentifier(const QString &filename, const QString &gdbVarDeclaration_, const QString &parentVariable, bool extractArrays)
{
  QString gdbVarDeclaration = gdbVarDeclaration_.trimmed();

  int arrayOpenBracePos = gdbVarDeclaration.indexOf("[");
  QString arrayItem;
  QString baseType;
  if (arrayOpenBracePos != -1) {
    // this is an array
    if (arrayOpenBracePos != gdbVarDeclaration.lastIndexOf("["))
      return; // multi dimension array

    auto variableName = gdbVarDeclaration.mid(gdbVarDeclaration.lastIndexOf(" ", arrayOpenBracePos) + 1, arrayOpenBracePos - gdbVarDeclaration.lastIndexOf(" ", arrayOpenBracePos) - 1).trimmed();
    if (variableName.lastIndexOf("*") != -1) {
      // strip pointer prefix
      variableName = variableName.mid(variableName.lastIndexOf("*") + 1).trimmed();
    }
    int arraySize = 1;
    if (extractArrays == true) {
      QString arraySizeStr = gdbVarDeclaration.mid(arrayOpenBracePos + 1, gdbVarDeclaration.indexOf("]", arrayOpenBracePos) - (arrayOpenBracePos + 1));
      arraySize = arraySizeStr.toInt();
    }

    arrayItem = parentVariable + variableName + "[0]";
    baseType = m_gdb->getTypeDesc(arrayItem);
    for (int m = 0; m < arraySize; m++) {
      arrayItem = parentVariable + variableName + "[" + m + "]";
      if (!baseType.isEmpty())
        ExtractType(filename, baseType, arrayItem, extractArrays);
    }
    return;
  }

  QString str3;
  if (gdbVarDeclaration.lastIndexOf("*") != -1) {
    // thsi is a pointer
    int k = gdbVarDeclaration.indexOf(")");
    if (k != -1) {
      gdbVarDeclaration = gdbVarDeclaration.left(k);
    }

    gdbVarDeclaration = gdbVarDeclaration.mid(gdbVarDeclaration.lastIndexOf("*") + 1).trimmed();

    if (gdbVarDeclaration.lastIndexOf(" ") != -1) {
      gdbVarDeclaration = gdbVarDeclaration.mid(gdbVarDeclaration.lastIndexOf(" ") + 1).trimmed();
    }
    if (gdbVarDeclaration.isEmpty())
      return;
    arrayItem = parentVariable + gdbVarDeclaration;
    str3 = gdbVarDeclaration;
  }
  else if (gdbVarDeclaration.contains("::"))
  {
    if (gdbVarDeclaration.contains("<"))
    {
      return;
    }
    if ((gdbVarDeclaration.startsWith("const ")) || (gdbVarDeclaration.startsWith("static const ")))
    {
      return;
    }

    str3 = gdbVarDeclaration.mid(gdbVarDeclaration.lastIndexOf(" ") + 1).trimmed();
    arrayItem = parentVariable + str3;
  } else {
    if ((gdbVarDeclaration.contains(":")) && (!gdbVarDeclaration.contains("::")))
    {
      return;
    }

    str3 = gdbVarDeclaration.mid(gdbVarDeclaration.lastIndexOf(" ") + 1).trimmed();
    arrayItem = parentVariable + str3;
  }

  gdbVarDeclaration = gdbVarDeclaration.trimmed();
  if (gdbVarDeclaration.endsWith(arrayItem))
    gdbVarDeclaration = gdbVarDeclaration.mid(0, gdbVarDeclaration.lastIndexOf(arrayItem));
  else if (gdbVarDeclaration.endsWith(str3)) {
    gdbVarDeclaration = gdbVarDeclaration.mid(0, gdbVarDeclaration.lastIndexOf(str3));
  }
  if (!ExtractType(filename, gdbVarDeclaration, arrayItem, extractArrays))
  {
    baseType = m_gdb->getTypeDesc(arrayItem);
    if (!baseType.isEmpty())
      ExtractType(filename, baseType, arrayItem, extractArrays);
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
