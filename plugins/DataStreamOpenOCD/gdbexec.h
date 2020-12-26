#pragma once

#include <QDebug>
#include <QProcess>
#include <QThread>

class InfoVar;

class GdbExec
{
public:
  enum Result {
    IO_ERROR = 0,
    CMD_ERROR = 1,
    CMD_IN_PROGRESS = 2,
    CMD_END_OK = 3,
  };

  enum GdbCommandResult {
    Idle,
    Error = 1,
    WaitForAnswer = 2,
    AnswerReceived = 3
  };


  GdbExec();
  ~GdbExec();

  bool isRunning() const;
  bool startGdb();

  QString getTypeDesc(const QString &  paramString);
  short getType(const QString &  paramString);
  int getSizeof(const QString &  paramString);
  bool buildVarList(const QString &  symbolFilePath, bool parambool);
  long getSymbolAddress(const QString &  paramString);
  void dump();

private:
  const QString GDB_PROMPT = "(gdb) ";
  const int GDB_PROMPT_LENGTH = 6;
  QProcess m_p;
  bool verbose = true;

  QString m_stdoutStr = "";
  QString m_stderrStr = "";
  long m_addressOffset = 0L;
  InfoVar *m_getGdbVar = nullptr;
  const QString m_gdbExecutablePath = "/usr/bin/gdb-multiarch";


  void sendCmd(const QString &  command);
  void stop();
  GdbCommandResult sendCmdAndWaitAnswer(const QString &  paramString);
};
