#include "infovar.h"

#include "gdbvar.h"

#include <QRegularExpression>
#include <QDebug>

InfoVar::InfoVar(GdbExec *paramGdbExec) :
  m_gdb(paramGdbExec)
{

}

void InfoVar::Parse(const QString m_infoVarStr, bool extendedParse)
{
  qDeleteAll(m_varList);
  m_varList.clear();
  if (m_infoVarStr.isEmpty())
    return;

  int fileSectionEndMarker = m_infoVarStr.indexOf(FILE_MARKER_START);
  bool finished = false;
  while ((fileSectionEndMarker != -1) && !finished) {
    int nextFileMarker = m_infoVarStr.indexOf(FILE_MARKER_START, fileSectionEndMarker + 1);
    if (nextFileMarker == -1) {
      nextFileMarker = m_infoVarStr.indexOf(NON_DEBUG_MARKER, fileSectionEndMarker + 1);
      finished = true;
    }

    if (nextFileMarker == -1) {
      // if no "Non-debugging symbols:" found at the end of the output
      // put the nextFileMarker index to the end of the output
      // BuildVarList will use this index
      nextFileMarker = m_infoVarStr.length() - 1;
      finished = true;
    }

    int fileNameEndPosition = m_infoVarStr.indexOf(FILE_MARKER_END, fileSectionEndMarker);
    if (fileNameEndPosition == -1) {
      qWarning() << "Info var parsing error while getting filename";
      return;
    }
    const auto fileName = m_infoVarStr.mid(fileSectionEndMarker + FILE_MARKER_START.length(), fileNameEndPosition - (fileSectionEndMarker + FILE_MARKER_START.length()));
    ProcessFileSection(fileName,
                       m_infoVarStr.mid(fileNameEndPosition + FILE_MARKER_END.length(), nextFileMarker - (fileNameEndPosition + FILE_MARKER_END.length())),
                       QString(),
                       extendedParse);
    fileSectionEndMarker = nextFileMarker;
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

void InfoVar::ProcessFileSection(const QString &fileName, const QString &variableList, const QString &paramString3, bool extendedParse)
{
  int processIndex = 0;
  while (processIndex < variableList.length()) {
    while (processIndex < variableList.length()) {
      if (variableList.at(processIndex).isSpace())
        processIndex++;
      else
        break;
    }
    if (!(processIndex < variableList.length()))
      break;
    // strip leading "128:    " like prefixes
    if (variableList.at(processIndex).isDigit()) {
      while (variableList.at(processIndex).isDigit() && processIndex < variableList.length()) {
        processIndex++;
      }

      if (variableList.at(processIndex) == QChar(':')) {
        processIndex++;
        while (variableList.at(processIndex).isSpace() && processIndex < variableList.length()) {
          processIndex++;
        }
      }
    }
    QString processedString = variableList.mid(processIndex);
    int commaPos = processedString.indexOf(";");

    int bracketOpenPos = processedString.indexOf("{");
    int closingBracePos = 0;
    if (bracketOpenPos != -1 && bracketOpenPos < commaPos) {
      closingBracePos = GetMatchingClosingBrace(processedString);
      if (closingBracePos == -1) {
        qWarning() << "Info var parsing error: opened brace not closed";
        return;
      }
    }

    int nextCommaPos = processedString.indexOf(";", closingBracePos);
    if (nextCommaPos == -1)
      return;
    QString declarationBody;
    if (closingBracePos != 0) {
      // line contained a {} construct pass only the contents between the {} pair
      declarationBody = processedString.mid(0, bracketOpenPos) + processedString.mid(closingBracePos + 1, nextCommaPos - closingBracePos - 1);
    } else {
      declarationBody = processedString.mid(0, nextCommaPos);
    }
    ExtractIdentifier(fileName, declarationBody, paramString3, extendedParse);
    processIndex += nextCommaPos + 1;
  }
}

void InfoVar::ExtractIdentifier(const QString &filename, const QString &gdbVarDeclaration_, const QString &parentVariable, bool extendedParse)
{
  QString variableDeclaration = gdbVarDeclaration_.trimmed();

  int arrayOpenBracePos = variableDeclaration.indexOf("[");
  QString arrayItem;
  QString baseType;
  if (arrayOpenBracePos != -1) {
    // this is an array
    if (arrayOpenBracePos != variableDeclaration.lastIndexOf("["))
      return; // multi dimension array ??
    auto lastSpacePos = variableDeclaration.lastIndexOf(" ", arrayOpenBracePos);
    auto variableName = variableDeclaration.mid(lastSpacePos + 1, arrayOpenBracePos - lastSpacePos - 1).trimmed();

    // strip pointer prefix if present
    if (variableName.contains('*'))
      variableName = variableName.mid(variableName.lastIndexOf("*") + 1).trimmed();

    int arraySize = 1;
    if (extendedParse == true) {
      QString arraySizeStr = variableDeclaration.mid(arrayOpenBracePos + 1, variableDeclaration.indexOf("]", arrayOpenBracePos) - (arrayOpenBracePos + 1));
      arraySize = arraySizeStr.toInt();
    }

    baseType = m_gdb->getTypeDesc(parentVariable + variableName + "[0]");
    for (int m = 0; m < arraySize; m++) {
      arrayItem = parentVariable + variableName + "[" + QString::number(m) + "]";
      if (!baseType.isEmpty())
        ExtractType(filename, baseType, arrayItem, extendedParse);
    }
    return;
  }

  QString str3;
  if (variableDeclaration.contains('*')) {
    // this is a pointer
    int closingBracketPos = variableDeclaration.indexOf(")");
    if (closingBracketPos != -1)
      variableDeclaration = variableDeclaration.mid(0, closingBracketPos);

    variableDeclaration = variableDeclaration.mid(variableDeclaration.lastIndexOf("*") + 1).trimmed();

    if (variableDeclaration.contains(' ')) {
      variableDeclaration = variableDeclaration.mid(variableDeclaration.lastIndexOf(" ") + 1).trimmed();
    }
    if (variableDeclaration.isEmpty())
      return;
    arrayItem = parentVariable + variableDeclaration;
    str3 = variableDeclaration;
  } else if (variableDeclaration.contains("::")) {
    if (variableDeclaration.contains("<"))
      return;

    if ((variableDeclaration.startsWith("const ")) || (variableDeclaration.startsWith("static const ")))
      return;

    str3 = variableDeclaration.mid(variableDeclaration.lastIndexOf(" ") + 1).trimmed();
    arrayItem = parentVariable + str3;
  } else {
    if ((variableDeclaration.contains(":")) && (!variableDeclaration.contains("::")))
      return;

    str3 = variableDeclaration.mid(variableDeclaration.lastIndexOf(" ") + 1).trimmed();
    arrayItem = parentVariable + str3;
  }

  variableDeclaration = variableDeclaration.trimmed();
  if (variableDeclaration.endsWith(arrayItem))
    variableDeclaration = variableDeclaration.mid(0, variableDeclaration.lastIndexOf(arrayItem));
  else if (variableDeclaration.endsWith(str3))
    variableDeclaration = variableDeclaration.mid(0, variableDeclaration.lastIndexOf(str3));

  // First try to get the type info from the GDB delaration
  // if it fails ask the GDB for the type description
  if (!ExtractType(filename, variableDeclaration, arrayItem, extendedParse)) {
    baseType = m_gdb->getTypeDesc(arrayItem);
    if (!baseType.isEmpty())
      ExtractType(filename, baseType, arrayItem, extendedParse);
  }
}

bool InfoVar::ExtractType(const QString &filename, const QString &variableDeclaration_, const QString &name, bool extendedParse)
{
  if (variableDeclaration_.contains("lastAckedSeqNum")) {
    qWarning() << "lastz";
  }
  QString variableDeclaration = variableDeclaration_.trimmed();
  // remove extern, static, volatile, const modifiers
  bool checkForRemovablePrefix = true;
  while (checkForRemovablePrefix) {
    checkForRemovablePrefix = false;
    if (variableDeclaration.startsWith("extern ")) {
      variableDeclaration = variableDeclaration.mid(7);
      checkForRemovablePrefix = true;
    }
    if (variableDeclaration.startsWith("static ")) {
      variableDeclaration = variableDeclaration.mid(7);
      checkForRemovablePrefix = true;
    }
    if (variableDeclaration.startsWith("volatile ")) {
      variableDeclaration = variableDeclaration.mid(9);
      checkForRemovablePrefix = true;
    }
    if (variableDeclaration.startsWith("const ")) {
      variableDeclaration = variableDeclaration.mid(6);
      checkForRemovablePrefix = true;
    }
  }

  if (variableDeclaration.startsWith("struct ") || variableDeclaration.startsWith("union ")) {
    int openingBracketPos = variableDeclaration.indexOf("{");
    int closingBracketPos = 0;
    if (openingBracketPos != -1) {
      closingBracketPos = GetMatchingClosingBrace(variableDeclaration);
      if (closingBracketPos == -1) {
        qWarning() << "Can not match opened brace in " + variableDeclaration;
        return false;
      }

      if (variableDeclaration.indexOf("*", closingBracketPos + 1) == -1) {
        ProcessFileSection(filename, variableDeclaration.mid(openingBracketPos + 1, closingBracketPos), name + '.', extendedParse);
        return true;
      }
    } else {
      // struct/union without starting { bracket
      return false;
    }
  }

  auto s = getType(variableDeclaration, name);
  if (s == Void)
    return false;

  // skip if already added to the list
  for (const auto *localGdbVar : m_varList) {
    if (localGdbVar->get_name() == name)
      return true;
  }

  long address = m_gdb->getSymbolAddress(name);
  m_varList.append(new GdbVar(name, s, filename, address));
  return true;
}

InfoVar::DataType InfoVar::getType(const QString &variableType_, const QString &paramString2)
{
  // TODO expand it with the stdint definitions to make it faster
  QString variableType = variableType_.trimmed();
  bool checkForRemovablePrefix = true;
  while (checkForRemovablePrefix) {
    checkForRemovablePrefix = false;
    if (variableType .startsWith("extern ")) {
      variableType  = variableType .mid(7);
      checkForRemovablePrefix = true;
    }
    if (variableType .startsWith("static ")) {
      variableType  = variableType .mid(7);
      checkForRemovablePrefix = true;
    }
    if (variableType .startsWith("volatile ")) {
      variableType  = variableType .mid(9);
      checkForRemovablePrefix = true;
    }
  }

  int gdbSize = -1;
  if (variableType .lastIndexOf("*") != -1) {
    gdbSize = m_gdb->getSizeof(paramString2);
    if (gdbSize == 1) {
      return Int8;
    } else if (gdbSize == 4) {
      return UInt32;
    } else {
      return UInt16;
    }
  } else if (variableType .startsWith("enum")) {
    gdbSize = m_gdb->getSizeof(paramString2);
    if (gdbSize == 1)
      return Int8;
    else
      return Int16;
  } else if (variableType  == "unsigned char") {
    return UInt8;
  } else if (variableType  == "char"
             || variableType  == "signed char") {
    return Int8;
  } else if (variableType  == "unsigned short"
             || variableType  == "unsigned short int"
             || variableType  == "short unsigned int") {
    return UInt16;
  } else if (variableType  == "short"
             || variableType  == "short int"
             || variableType  == "signed short"
             || variableType  == "signed short int"
             || variableType  == "short signed int") {
    return Int16;
  } else if (variableType  == "unsigned int") {
    gdbSize = m_gdb->getSizeof(paramString2);
    if (gdbSize == 2)
      return UInt16;
    else
      return UInt32;
  } else if (variableType  == "int"
             || variableType  == "signed int") {
    gdbSize = m_gdb->getSizeof(paramString2);
    if (gdbSize == 2)
      return Int16;
    else
      return Int32;
  } else if (variableType  == "unsigned long"
             || variableType  == "unsigned long int"
             || variableType  == "long unsigned int") {
    return UInt32;
  } else if (variableType  == "long"
             || variableType  == "long int"
             || variableType  == "signed long"
             || variableType  == "signed long int"
             || variableType  == "long signed int") {
    return Int32;
  } else {
    if (variableType .startsWith("void"))
      return Void;
    if (variableType .contains("double"))
      return Double;
    if (variableType .contains("float"))
      return Float;
  }
  return Void;
}

void InfoVar::dump()
{
  for (auto var : m_varList) {
    var->dump();
  }
}
