#pragma once

#include "gdbexec.h"

#include <QString>
#include <QStringList>

class GdbExec;
class GdbVar;

class InfoVar
{
public:
  enum DataType {
    Void = -1,
    Signed = 1,
    UInt8 = 0,
    Int8 = (UInt8 | Signed),
    UInt16 = 2,
    Int16 = (UInt16 | Signed),
    UInt32 = 4,
    Int32 = (UInt32 | Signed),
    Double = 7,
    Float = 6
  };
  InfoVar(GdbExec *paramGdbExec);
  void Parse(const QString m_infoVarStr, bool extendedParse);
  DataType getType(const QString  &variableType_, const QString &paramString2);
  void dump();

private:
  QList<GdbVar*> m_varList;
  GdbExec *m_gdb;
  QString FILE_MARKER_START = "File ";
  const QString FILE_MARKER_END = ":\n";
  const QString NON_DEBUG_MARKER = "Non-debugging symbols:";

  int GetMatchingClosingBrace(const QString &paramString);
  void ProcessFileSection(const QString & fileName, const QString & variableList, const QString & paramString3, bool extendedParse);
  void ExtractIdentifier(const QString & filename, const QString & paramString2, const QString & parentVariable, bool extendedParse);
  bool ExtractType(const QString & filename, const QString & paramString2, const QString & paramString3, bool extendedParse);
};
