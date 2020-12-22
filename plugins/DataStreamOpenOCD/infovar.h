#pragma once

#include "gdbexec.h"

#include <QString>
#include <QStringList>

class GdbExec;
class GdbVar;

class InfoVar
{
public:
  InfoVar(GdbExec *paramGdbExec);
  void Parse(const QString &m_infoVarStr, bool parambool);
  short getType(const QString  &paramString1, const QString &paramString2);

private:
  QList<GdbVar*> m_varList;
  GdbExec *m_gdb;
  QString FILE_MARKER_START = "File ";
  const QString FILE_MARKER_END = ":\n";
  const QString NON_DEBUG_MARKER = "Non-debugging symbols:";

  int GetMatchingClosingBrace(const QString &paramString);
  void BuildVarList(const QString & fileName, const QString & paramString2, const QString & paramString3, bool parambool);
  void ExtractIdentifier(const QString & filename, const QString & paramString2, const QString & parentVariable, bool parambool);
  bool ExtractType(const QString & paramString1, const QString & paramString2, const QString & paramString3, bool parambool);
};
